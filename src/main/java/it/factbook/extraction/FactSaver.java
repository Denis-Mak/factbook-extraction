package it.factbook.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.factbook.dictionary.LangDetector;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.message.DocumentMessage;
import it.factbook.extraction.message.FactsMessage;
import it.factbook.extraction.util.WebHelper;
import it.factbook.search.Fact;
import it.factbook.search.FactProcessor;
import it.factbook.search.repository.FactAdapter;
import it.factbook.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Component
public class FactSaver implements MessageListener{
    private static ObjectMapper jsonMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(FactSaver.class);

    @Autowired
    FactProcessor factProcessor;

    @Autowired
    FactAdapter factAdapter;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private AmqpConfig amqpConfig;

    @Autowired
    private LangDetector langDetector;

    @Override
    public void onMessage(Message jsonMsg) {
        DocumentMessage msg = new DocumentMessage();
        try {
            msg = jsonMapper.readValue(jsonMsg.getBody(), DocumentMessage.class);
            if (msg.getContent() == null || msg.getContent().length() < 1){
                log.warn("Received empty message. URL: {} Title: {}", msg.getUrl(), msg.getTitle());
            }
            msg.setContent(removeHyphens(StringUtils.removeControlCharacters(msg.getContent())));   // remove all special chars
        } catch (IOException e) {
            log.error("Error during unpack DocumentMessage: ", e);
        }

        log.debug("Received message. URL: {} \n Title: {}", msg.getUrl(), msg.getTitle());
        Fact documentHeader = buildDocHeader(msg);
        long docId = factAdapter.saveDocumentHeader(documentHeader);
        factAdapter.saveDocumentContent(docId, msg.getContent());
        factAdapter.appendFacts(buildListOfFacts(docId, msg));
        List<Fact> factsWithId = factAdapter.getByDocId(docId);
        List<Fact> factsWithDocHeader = factsWithId.stream()
                .map(f -> new Fact.Builder(documentHeader)
                        .id(f.getId())
                        .content(f.getContent())
                        .contentSense(f.getContentSense())
                        .factLang(f.getFactLang())
                        .docId(f.getDocId())
                        .docPosition(f.getDocPosition())
                        .fingerprint(f.getFingerprint())
                        .factuality(f.getFactuality())
                        .build())
                .collect(Collectors.toList());
        FactsMessage factsMessage = new FactsMessage();
        factsMessage.setFacts(factsWithDocHeader);
        passFactsToClusterProcessor(factsMessage);
    }

    private void passFactsToClusterProcessor(FactsMessage factsMessage) {
        try {
            jsonMapper.registerModule(new JodaModule());
            String json = jsonMapper.writeValueAsString(factsMessage);
            amqpTemplate.convertAndSend(AmqpConfig.indexUpdaterExchange().getName(), "#", json);
        } catch (JsonProcessingException e) {
            log.error("Error converting FactMessage: ", e);
        }
    }

    static String removeHyphens(String str){
        if (str == null){
            return null;
        }
        char[] buf = new char[str.length()];
        int i = 0;
        for (char each:str.toCharArray()) {
            if (each != '¬') {
                buf[i++] = each;
            }
        }
        if (i == str.length()) {
            return str;
        } else {
            return new String(Arrays.copyOfRange(buf, 0, i));
        }
    }

    Fact buildDocHeader(DocumentMessage msg){
        return new Fact.Builder()
                .title(msg.getTitle())
                .titleSense(factProcessor.convertToSense(factProcessor.splitWords(msg.getTitle()), msg.getGolem()))
                .docUrl(WebHelper.getDecodedURL(msg.getUrl()))
                .docLang(langDetector.detectLanguage(msg.getContent()))
                .golem(msg.getGolem()).build();
    }

    List<Fact> buildListOfFacts(long docId, DocumentMessage msg){
        List<Fact> facts = new ArrayList<>();
        int startPos = 0;
        for (String textBlock:msg.getContent().split("\n")){
            if (StringUtils.trimSplitters(textBlock).trim().length() > 0) {
                List<Fact> factsInBlock = factProcessor.splitDocument(textBlock, docId, msg.getGolem(), startPos);
                if (factsInBlock.size() > 0) {
                    facts.addAll(factsInBlock);
                    startPos = facts.get(facts.size()-1).getDocPosition() + 1;
                }
            }
        }
        return facts;
    }
}
