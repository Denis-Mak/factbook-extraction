package it.factbook.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.factbook.extraction.message.FactsMessage;
import it.factbook.search.classifier.Classifier;
import it.factbook.search.classifier.FactCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
public class FactClassifierTrainer{
    private static ObjectMapper jsonMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(FactClassifierTrainer.class);

    @Autowired
    Classifier classifier;

    public void onMessageHam(String jsonMsg) {
       // processMessage(jsonMsg, FactCategory.HAM);
    }

    public void onMessageGarbage(String jsonMsg) {
       // processMessage(jsonMsg, FactCategory.GARBAGE);
    }

    private void processMessage(String jsonMsg, FactCategory category){
        FactsMessage msg = new FactsMessage();
        try {
            msg = jsonMapper.readValue(jsonMsg, FactsMessage.class);
        } catch (IOException e) {
            log.error("Error during unpack DocumentMessage: ", e);
        }
        if (msg.getFacts() != null) {
            classifier.tweakClassifier(msg.getFacts(), category);
        }
    }
}
