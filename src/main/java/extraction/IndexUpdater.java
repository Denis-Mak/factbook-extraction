package extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.factbook.search.repository.DocumentRepositoryConfig;
import it.factbook.sphinx.SphinxIndexUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
public class IndexUpdater implements MessageListener{
    private static ObjectMapper jsonMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(IndexUpdater.class);

    @Autowired
    SphinxIndexUpdater sphinxIndexUpdater;

    @Autowired
    DocumentRepositoryConfig config;

    @Override
    public void onMessage(Message message) {
        try {
            jsonMapper.registerModule(new JodaModule());
            FactsMessage msg = jsonMapper.readValue(message.getBody(), FactsMessage.class);
            log.debug("Received message. Facts count: {}", msg.getFacts().size());
            int golemId = msg.getFacts().get(0).getGolemId();
            sphinxIndexUpdater.updateIndex(msg.getFacts(), config.rtIndexes.get(golemId).get(0));
        } catch (IOException e) {
            log.error("Error during unpack DocumentMessage: {}", e);
        }
    }
}
