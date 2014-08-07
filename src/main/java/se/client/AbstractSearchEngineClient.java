package se.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.AmqpConfig;
import extraction.CrawlerLog;
import extraction.ProfileMessage;
import extraction.SearchResultsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
public class AbstractSearchEngineClient {
    private static final Logger log = LoggerFactory.getLogger(AbstractSearchEngineClient.class);
    static ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    CrawlerLog crawlerLog;

    protected ProfileMessage unpackProfileMessage(Message message){
        ProfileMessage profileMessage = null;
        try {
            profileMessage = jsonMapper.readValue(message.getBody(), ProfileMessage.class);
            log.debug("Received message. Profile ID: {}, {} lines.", profileMessage.getProfileId(), profileMessage.getQueryLines().size());
        } catch (IOException e) {
            log.error("Error during unpack ProfileMessage: {}", e);
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
