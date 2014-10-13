package it.factbook.extraction.client;

import com.fasterxml.jackson.databind.JsonNode;
import it.factbook.extraction.Link;
import it.factbook.extraction.message.ProfileMessage;
import it.factbook.extraction.message.SearchResultsMessage;
import it.factbook.extraction.util.WebHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
public class YahooClient extends AbstractSearchEngineClient implements MessageListener {
    private static final SearchEngine SEARCH_ENGINE = SearchEngine.YAHOO;
    private static final Logger log = LoggerFactory.getLogger(YahooClient.class);

    @Value("${yahoo.client.key}")
    private String clientKey;

    @Value("${yahoo.client.secret}")
    private String clientSecret;

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
            crawlerLog.logReturnedResults(requestLogId, foundLinks.size(), linksToCrawl.size());
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

    List<Link> getLinks(Query query){
        List<Link> links = new ArrayList<>(50);
        try {
            JsonNode root = jsonMapper.readTree(WebHelper.getContentOAuth(buildUrl(query), clientKey, clientSecret));
            JsonNode results = root.path("bossresponse").path("web").path("results");
            Iterator<JsonNode> itr = results.elements();
            while (itr.hasNext()) {
                JsonNode res = itr.next();
                links.add(new Link(res.path("url").textValue(),
                        res.path("title").textValue(),
                        res.path("abstract").textValue(),
                        query.golem));
            }
        } catch (IOException e) {
            log.error("Error on request: {} ", query.query);
            log.error("Error info:", e);
        }
        return links;
    }

    private String buildUrl(Query query){
        String encQuery = null;
        try {
            encQuery = URLEncoder.encode(query.query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported Encoding!");
        }

        return "https://yboss.yahooapis.com/ysearch/web?q=" + encQuery;
                //"&view=" + query.golem.getMainLang().getCode().toLowerCase() +
                //"&key=" + clientKey +
                //"&format=json";
    }
}
