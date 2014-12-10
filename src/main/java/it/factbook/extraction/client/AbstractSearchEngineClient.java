package it.factbook.extraction.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.factbook.dictionary.Golem;
import it.factbook.dictionary.LangDetector;
import it.factbook.dictionary.WordForm;
import it.factbook.extraction.CrawlerLog;
import it.factbook.extraction.Link;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.message.ProfileMessage;
import it.factbook.extraction.message.SearchResultsMessage;
import org.slf4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

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

    @Autowired
    LangDetector langDetector;

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

    protected void sendToCrawler(long profileId, SearchEngine searchEngine, long requestLogId, List<Link> linksToCrawl){
        if (linksToCrawl == null || linksToCrawl.size() < 1) {
            return;
        }
        crawlerLog.logFoundLinks(profileId, searchEngine, requestLogId, linksToCrawl);
        SearchResultsMessage searchResultsMessage = new SearchResultsMessage(linksToCrawl);
        searchResultsMessage.setProfileId(profileId);
        searchResultsMessage.setSearchEngine(searchEngine);
        searchResultsMessage.setRequestLogId(requestLogId);
        try {
            String json = jsonMapper.writeValueAsString(searchResultsMessage);
            amqpTemplate.convertAndSend(AmqpConfig.crawlerExchange().getName(), "#", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    protected void passResultsToCrawler(SearchResultsMessage msg){
        try {
            String json = jsonMapper.writeValueAsString(msg);
            amqpTemplate.convertAndSend(AmqpConfig.crawlerExchange().getName(), "#", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public List<Query> getQueries(ProfileMessage profileMessage) {
        final int WORDGRAMS_IN_ONE_QUERY = 5;
        String initialQuery =
                (profileMessage.getInitialQuery() != null && profileMessage.getInitialQuery().length() > 0) ?
                        profileMessage.getInitialQuery() : "";
        if (profileMessage.getQueryLines() != null && profileMessage.getQueryLines().size() > 0) {
            List<Query> wordgramQueries = new ArrayList<>();
            StringJoiner sj = new StringJoiner(" | ","(", ")");
            for (List<List<WordForm>> line: profileMessage.getQueryLines()){
                for(List<WordForm> wordgram: line){
                    for (WordForm word : wordgram) {
                        sj.add(word.getWord());
                    }
                    if (wordgram.get(0).getGolem() != Golem.UNKNOWN) {
                        wordgramQueries.add(new Query(wordgram.get(0).getGolem(), sj.toString()));
                    }
                    sj = new StringJoiner(" | ","(", ")");
                }
            }
            Collections.sort(wordgramQueries, (q1, q2) -> q1.golem.getId() - q2.golem.getId());
            List<Query> queries = new ArrayList<>();
            int wordgramCounter = 0;
            Golem golem = Golem.UNKNOWN;
            sj = new StringJoiner(" | ","(", ")");
            for (Query each:wordgramQueries){
                if (wordgramCounter == 0) golem = each.golem;
                if (each.golem != golem || wordgramCounter >= WORDGRAMS_IN_ONE_QUERY){
                    wordgramCounter = 0;
                    queries.add(new Query(golem, initialQuery + " " + sj.toString()));
                    sj = new StringJoiner(" | ","(", ")");
                    golem = each.golem;
                }

                wordgramCounter++;
                sj.add(each.query);
            }
            queries.add(new Query(golem, initialQuery + " " + sj.toString()));
            return queries;
        } else if (!"".equals(initialQuery)) {
            Query query = new Query(predictGolem(initialQuery), initialQuery);
            return Arrays.asList(query);
        } else {
            return Collections.<Query>emptyList();
        }

    }

    protected Golem predictGolem(String str){
        switch (langDetector.detectLanguage(str)){
            case RU:
                return Golem.WIKI_RU;
            case EN:
                return Golem.WIKI_EN;
            default:
                return Golem.UNKNOWN;
        }
    }
}
