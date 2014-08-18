package it.factbook.extraction.client;

import com.fasterxml.jackson.databind.JsonNode;
import it.factbook.dictionary.WordForm;
import it.factbook.extraction.Link;
import it.factbook.extraction.ProfileMessage;
import it.factbook.extraction.SearchResultsMessage;
import it.factbook.extraction.util.WebHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class BingClient extends AbstractSearchEngineClient implements MessageListener {
    private static final SearchEngine SEARCH_ENGINE = SearchEngine.BING;
    private static final Logger log = LoggerFactory.getLogger(BingClient.class);
    private static final String appKey = "***REMOVED***";

    @Override
    protected Logger log() {
        return log;
    }

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

    List<Query> getQueries(ProfileMessage profileMessage) {
        int WORDGRAMS_IN_ONE_QUERY = 5;
        List<Query> wordgramQueries = new ArrayList<>();
        for (List<List<WordForm>> line: profileMessage.getQueryLines()){
            for(List<WordForm> wordgram: line){
                String query = "";
                query += "(";
                for (WordForm word : wordgram) {
                    query += word.getWord() + " ";
                }
                query = query.substring(0, query.length()-1) + ")";
                wordgramQueries.add(new Query(wordgram.get(0).getGolemId(), query));
            }
        }
        Collections.sort(wordgramQueries, (q1, q2) -> q1.golemId - q2.golemId);
        List<Query> queries = new ArrayList<>();
        String query = "";
        int wordgramCounter = 0;
        int golemId = 0;
        boolean hasInitialQuery = profileMessage.getInitialQuery() != null && profileMessage.getInitialQuery().length() > 0;
        for (Query each:wordgramQueries){
            if (wordgramCounter > 0 && (each.golemId != golemId || wordgramCounter >= WORDGRAMS_IN_ONE_QUERY)){
                wordgramCounter = 0;
                query = query.substring(0, query.length()-3);
                if (hasInitialQuery) query += ")";
                queries.add(new Query(golemId, query));
            }
            if (wordgramCounter == 0){
                golemId = each.golemId;
                if (hasInitialQuery){
                    query = profileMessage.getInitialQuery() + " (";
                } else {
                    query = "";
                }
            }
            wordgramCounter++;
            query += each.query + " | ";
        }
        query = query.substring(0, query.length()-3);
        if (hasInitialQuery) query += ")";
        queries.add(new Query(golemId, query));

        return queries;
    }

    List<Link> getLinks(Query query){
        List<Link> links = new ArrayList<>(100);
        try {
            JsonNode root = jsonMapper.readTree(WebHelper.getUrl(buildUrl(query.query), null, appKey, appKey));
            JsonNode results = root.path("d").path("results").get(0).path("Web");
            Iterator<JsonNode> itr = results.elements();
            while (itr.hasNext()) {
                JsonNode res = itr.next();
                links.add(new Link( res.path("Url").textValue(),
                        res.path("Title").textValue(),
                        res.path("Description").textValue(),
                        query.golemId));
            }
        } catch (IOException e) {
            log.error("Error on request: {} ", query.query);
            log.error("Error info:", e);
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
        return "https://api.datamarket.azure.com/Bing/Search/Composite?"

                // Common request fields (required)
                + "Query=%27" + encQuery + "%27"
                + "&Sources=%27web%27"

                // Web-specific request fields (optional)
                //+ "&$top=10"
                //+ "&$skip=0"
                //+ "&Options=%27DisableLocationDetection%2BEnableHighlighting%27 "

                // JSON-specific request fields (optional)
                + "&$format=json";
    }
}
