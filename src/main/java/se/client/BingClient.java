package se.client;

import com.fasterxml.jackson.databind.JsonNode;
import extraction.Link;
import extraction.ProfileMessage;
import extraction.SearchResultsMessage;
import it.factbook.dictionary.WordForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import util.WebHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 *
 */
public class BingClient extends AbstractSearchEngineClient implements MessageListener {
    private static final SearchEngine SEARCH_ENGINE = SearchEngine.BING;
    private static final Logger log = LoggerFactory.getLogger(BingClient.class);
    private static final String appKey = "***REMOVED***";

    @Override
    public void onMessage(Message message) {
        ProfileMessage profileMessage = unpackProfileMessage(message);
        long profileVersion = System.currentTimeMillis();
        List<Query> queries = getQueries(profileMessage);
        for(Query query: queries){
            List<Link> foundLinks = getLinks(query);
            List<Link> linksToCrawl = crawlerLog.getLinksToCrawl(foundLinks);
            if (linksToCrawl.size() > 0) {
                crawlerLog.logFoundLinks(profileMessage.getProfileId(), SEARCH_ENGINE, profileVersion, foundLinks);
                SearchResultsMessage searchResultsMessage = new SearchResultsMessage(linksToCrawl);
                searchResultsMessage.setProfileId(profileMessage.getProfileId());
                searchResultsMessage.setSearchEngine(SEARCH_ENGINE);
                searchResultsMessage.setProfileVersion(profileVersion);
                passResultsToCrawler(searchResultsMessage);
            }
        }
    }

    List<Query> getQueries(ProfileMessage profileMessage) {
        Map<Integer,String> queriesMap = new HashMap<>(); //query for each golem executes separately to distinct results
        for (List<List<WordForm>> line: profileMessage.getQueryLines()){
            int golemId = line.get(0).get(0).getGolemId();
            String query = queriesMap.get(golemId);
            if (query == null) query = "";
            for(List<WordForm> wordgram: line){
                if (SEARCH_ENGINE.containsGolemId(golemId)) {
                    if (line.size() > 1) query += "(";
                    query += "(";
                    for (WordForm word : wordgram) {
                        query += word.getWord() + " ";
                    }
                    query = query.substring(0, query.length()-1);
                    query += ") | ";
                }
            }
            query = query.substring(0, query.length()-3); //cut last column
            query += " | ";
            queriesMap.put(golemId, query);
        }

        List<Query> queries = new ArrayList<>(queriesMap.size());
        for (Map.Entry<Integer,String> entry:queriesMap.entrySet()){
            String query = entry.getValue().substring(0, entry.getValue().length() - 3);
            if (profileMessage.getInitialQuery() != null && profileMessage.getInitialQuery().length() >0){
                query = profileMessage.getInitialQuery() + "(" + query + ")";
            }
            queries.add(new Query(entry.getKey(), query));
        }

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
