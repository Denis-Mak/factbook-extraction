package it.factbook.extraction.client;

import com.fasterxml.jackson.databind.JsonNode;
import it.factbook.extraction.Link;
import it.factbook.extraction.ProfileMessage;
import it.factbook.extraction.SearchResultsMessage;
import it.factbook.dictionary.WordForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;
import it.factbook.extraction.util.WebHelper;

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
public class FarooClient extends AbstractSearchEngineClient implements MessageListener{
    private static final SearchEngine SEARCH_ENGINE = SearchEngine.FAROO;
    static Logger log = LoggerFactory.getLogger(FarooClient.class);
    private static final String appKey = "***REMOVED***";

    @Override
    protected Logger log() {
        return log;
    }

    private static long lastAccessed = 0;

    @Override
    public void onMessage(Message message) {
        ProfileMessage profileMessage = unpackProfileMessage(message);
        long profileVersion = System.currentTimeMillis();
        List<Query> queries = getQueries(profileMessage);
        for(Query query: queries){
            long requestLogId = crawlerLog.logSearchRequest(profileMessage.getProfileId(), SEARCH_ENGINE, profileVersion, query);
            List<Link> foundLinks = getLinks(query);
            List<Link> linksToCrawl = crawlerLog.getLinksToCrawl(foundLinks);
            if (linksToCrawl.size() > 0) {
                crawlerLog.logFoundLinks(profileMessage.getProfileId(), SEARCH_ENGINE, requestLogId, foundLinks);
                SearchResultsMessage searchResultsMessage = new SearchResultsMessage(linksToCrawl);
                searchResultsMessage.setProfileId(profileMessage.getProfileId());
                searchResultsMessage.setSearchEngine(SEARCH_ENGINE);
                searchResultsMessage.setRequestLogId(requestLogId);
                passResultsToCrawler(searchResultsMessage);
            }
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
            pauseBetweenRequests();
            JsonNode root = jsonMapper.readTree(WebHelper.getUrl(buildUrl(query.query)));
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
        return "http://www.faroo.com/api?q=" + encQuery + "&start=1&length=10&l=en&src=news&i=false&f=json&key=" + appKey;
    }

    private void pauseBetweenRequests() {
        long timeToWait = 0;
        if(lastAccessed > 0)
            timeToWait = 1000 - (System.currentTimeMillis() - lastAccessed);

        if(timeToWait > 0) {
            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        lastAccessed = System.currentTimeMillis();
    }
}
