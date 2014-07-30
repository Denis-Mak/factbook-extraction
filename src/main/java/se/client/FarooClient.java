package se.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import extraction.CrawlerLog;
import extraction.Link;
import extraction.ProfileMessage;
import extraction.SearchResultsMessage;
import it.factbook.dictionary.WordForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.WebHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
@Component
public class FarooClient implements MessageListener {
    private static final SearchEngine SEARCH_ENGINE = SearchEngine.FAROO;
    private static final Logger log = LoggerFactory.getLogger(FarooClient.class);
    private static ObjectMapper jsonMapper = new ObjectMapper();

    private class Query{
        private int golemId;
        private String query;

        Query(int golemId, String query){
            this.golemId    = golemId;
            this.query      = query;
        }
    }

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private CrawlerLog crawlerLog;

    @Override
    public void onMessage(Message message) {
        try {
            ProfileMessage profileMessage = jsonMapper.readValue(message.getBody(), ProfileMessage.class);
            long profileVersion = System.currentTimeMillis();
            List<Query> queries = getQueries(profileMessage);
            for(Query query: queries){
                List<Link> foundLinks = getLinks(query);
                List<Link> linksToCrawl = crawlerLog.getLinksToCrawl(foundLinks);
                SearchResultsMessage searchResultsMessage = new SearchResultsMessage(linksToCrawl);
                searchResultsMessage.setProfileId(profileMessage.getProfileId());
                searchResultsMessage.setSearchEngine(SEARCH_ENGINE);
                searchResultsMessage.setProfileVersion(profileVersion);
                passResultsToCrawler(searchResultsMessage);
                crawlerLog.logFoundLinks(profileMessage.getProfileId(), SEARCH_ENGINE, profileVersion, foundLinks);
            }
        } catch (IOException e) {
            log.error("Error during unpack ProfileMessage: {}", e);
        }
    }

    List<Query> getQueries(ProfileMessage msg){
        List<Query> queries = new ArrayList<>(50);
        for (List<List<WordForm>> line: msg.getQueryLines()){
            for(List<WordForm> wordgram: line){
                int golemId = wordgram.get(0).getGolemId();
                if (SEARCH_ENGINE.containsGolemId(golemId)) {
                    String query = msg.getInitialQuery() != null ? msg.getInitialQuery() : "";
                    for (WordForm word : wordgram) {
                        query += " " + word.getWord();
                    }
                    queries.add(new Query(golemId, query));
                }
            }
        }
        return queries;
    }

    List<Link> getLinks(Query query){
        List<Link> links = new ArrayList<>(100);
        try {
            JsonNode root = jsonMapper.readTree(WebHelper.getUrl(buildUrl(query.query), "factbook-robot"));
            JsonNode results = root.path("results");
            Iterator<JsonNode> itr = results.elements();
            while (itr.hasNext()) {
                JsonNode res = itr.next();
                links.add(new Link( res.path("url").textValue(),
                                    res.path("title").textValue(),
                                    res.path("kwic").textValue(),
                                    query.golemId));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return links;
    }

    private String buildUrl(String query){
        String encQuery = null;
        try {
            encQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported Encoding!");
        }
        return "http://www.faroo.com/api?q=" + encQuery + "&start=1&length=10&l=en&src=web&i=false&f=json&key=***REMOVED***";
    }

    public void passResultsToCrawler(SearchResultsMessage msg){
        amqpTemplate.convertAndSend("crawler-query", msg);
    }
}
