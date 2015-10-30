package it.factbook.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.factbook.dictionary.Golem;
import it.factbook.extraction.message.FactsMessage;
import it.factbook.search.Fact;
import it.factbook.search.repository.DocumentRepositoryConfig;
import it.factbook.search.repository.SemanticSearch;
import it.factbook.search.repository.jdbc.SphinxSliceIndexAdapter;
import it.factbook.search.repository.sphinx.SphinxIndexUpdater;
import it.factbook.util.BitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Updates context and semantic search indexes calling associated adapters
 */
@Component
public class IndexUpdater implements MessageListener{

    @Autowired
    SphinxIndexUpdater sphinxIndexUpdater;

    @Autowired
    private SphinxSliceIndexAdapter sphinxSliceIndexAdapter;

    @Autowired
    private SemanticSearch semanticSearch;

    @Autowired
    DocumentRepositoryConfig config;

    private static ObjectMapper jsonMapper = new ObjectMapper();
    static {
        jsonMapper.registerModule(new JodaModule());
    }

    private static final Logger log = LoggerFactory.getLogger(IndexUpdater.class);

    /**
     * Handles incoming message and coordinates index updating.
     *
     * @param message an instance of {@link FactsMessage}
     */
    @Override
    public void onMessage(Message message) {
        try {
            FactsMessage msg = jsonMapper.readValue(message.getBody(), FactsMessage.class);
            if (msg.getFacts().size() > 0) {
                log.debug("Received message. Facts count: {}", msg.getFacts().size());
                Golem golem = msg.getFacts().get(0).getGolem();
                List<Fact> factsToInsert = msg.getFacts().stream()
                        .filter(f -> BitUtils.convertToInt(f.getFingerprint()) > 0)
                        .collect(Collectors.toList());
                sphinxIndexUpdater.updateIndex(factsToInsert, config.rtIndexes.get(golem).get(0));
                // update cassandra indexes
                factsToInsert.stream().forEach(
                        fact -> {
                            sphinxSliceIndexAdapter.addFactToIndex(fact.getUrlObj(), fact.getPos(), fact.getGolem());
                            semanticSearch.updateIndex(fact);
                        });
            }
        } catch (IOException e) {
            log.error("Error during unpack DocumentMessage: {}", e);
        }
    }
}
