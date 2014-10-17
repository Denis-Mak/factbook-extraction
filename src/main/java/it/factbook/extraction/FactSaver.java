package it.factbook.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.factbook.dictionary.LangDetector;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.message.DocumentMessage;
import it.factbook.extraction.message.FactsMessage;
import it.factbook.search.Fact;
import it.factbook.search.FactProcessor;
import it.factbook.search.repository.FactAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    public void onMessage(Message message) {
        DocumentMessage msg = new DocumentMessage();
        try {
            msg = jsonMapper.readValue(message.getBody(), DocumentMessage.class);
            msg.setContent(msg.getContent().replaceAll("\\p{C}", ""));   // remove all special chars
        } catch (IOException e) {
            log.error("Error during unpack DocumentMessage: {}", e);
        }

        log.debug("Received message. Document URL: {} \n Document title: {}", msg.getUrl(), msg.getTitle());
        Fact documentHeader = new Fact.Builder()
                .title(msg.getTitle())
                .titleSense(factProcessor.convertToSense(factProcessor.splitWords(msg.getTitle()), msg.getGolem()))
                .docUrl(msg.getUrl())
                .docLang(langDetector.detectLanguage(msg.getContent()))
                .golem(msg.getGolem()).build();
        long docId = factAdapter.saveDocumentHeader(documentHeader);
        factAdapter.saveDocumentContent(docId, msg.getContent());
        List<Fact> facts = factProcessor.splitDocument(msg.getContent(), docId, msg.getGolem());
        factAdapter.appendFacts(facts);
        List<Fact> factsWithId = factAdapter.getByDocId(docId);
        List<Fact> factsWithDocHeader = factsWithId.stream()
                .map(f -> new Fact.Builder(documentHeader)
                        .id(f.getId())
                        .content(f.getContent())
                        .contentSense(f.getContentSense())
                        .factLang(f.getFactLang())
                        .docId(f.getDocId())
                        .docPosition(f.getDocPosition())
                        .factFingerprint(f.getFactFingerprint())
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
            log.error("Error converting FactMessage: {}", e);
        }
    }
}
