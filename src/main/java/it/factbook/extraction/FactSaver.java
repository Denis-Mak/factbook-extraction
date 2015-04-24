package it.factbook.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.factbook.dictionary.LangDetector;
import it.factbook.dictionary.Language;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.message.DocumentMessage;
import it.factbook.extraction.message.FactsMessage;
import it.factbook.extraction.util.WebHelper;
import it.factbook.search.DocType;
import it.factbook.search.Fact;
import it.factbook.search.FactProcessor;
import it.factbook.search.repository.FactAdapter;
import it.factbook.util.StringUtils;
import org.joda.time.DateTime;
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

/**
 *
 */
@Component
public class FactSaver implements MessageListener{

    @Autowired
    FactProcessor factProcessor;

    @Autowired
    FactAdapter factAdapter;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private LangDetector langDetector;

    private static ObjectMapper jsonMapper = new ObjectMapper();
    static {
        jsonMapper.registerModule(new JodaModule());
    }

    private static final Logger log = LoggerFactory.getLogger(FactSaver.class);

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
        List<Fact> facts = buildListOfFacts(msg);
        factAdapter.appendFacts(facts);
        FactsMessage factsMessage = new FactsMessage();
        factsMessage.setFacts(facts);
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

    List<Fact> buildListOfFacts(DocumentMessage msg){
        List<Fact> facts = new ArrayList<>();
        String title = msg.getTitle();
        String url = WebHelper.getDecodedURL(msg.getUrl());
        Language language = langDetector.detectLanguage(msg.getContent());
        DateTime published = msg.getPublished();
        DocType docType = msg.getDocType();
        int startPos = 0;
        for (String textBlock:msg.getContent().split("\n")){
            if (StringUtils.trimSplitters(textBlock).trim().length() > 0) {
                List<Fact> factsInBlock = factProcessor.splitDocument(textBlock, msg.getGolem(), startPos, title,
                        url, language, published, docType);
                if (factsInBlock.size() > 0) {
                    facts.addAll(factsInBlock);
                    startPos = facts.get(facts.size()-1).getPos() + 1;
                }
            }
        }
        return facts;
    }
}
