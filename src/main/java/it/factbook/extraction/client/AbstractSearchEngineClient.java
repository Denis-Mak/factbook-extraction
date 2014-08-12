package it.factbook.extraction.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.CrawlerLog;
import it.factbook.extraction.ProfileMessage;
import it.factbook.extraction.SearchResultsMessage;
import org.slf4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
public abstract class AbstractSearchEngineClient {
    protected abstract Logger log();
    static ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    CrawlerLog crawlerLog;

    protected ProfileMessage unpackProfileMessage(Message message){
        ProfileMessage profileMessage = null;
        try {
            profileMessage = jsonMapper.readValue(message.getBody(), ProfileMessage.class);
            log().debug("Received message. Profile ID: {}, {} lines.", profileMessage.getProfileId(), profileMessage.getQueryLines().size());
        } catch (IOException e) {
            log().error("Error during unpack ProfileMessage: {}", e);
        }
        return profileMessage;
    }

    protected void passResultsToCrawler(SearchResultsMessage msg){
        try {
            String json = jsonMapper.writeValueAsString(msg);
            amqpTemplate.convertAndSend(AmqpConfig.crawlerExchange().getName(), "#", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
