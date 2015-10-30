package it.factbook.extraction.client;

import com.fasterxml.jackson.databind.JsonNode;
import it.factbook.extraction.Link;
import it.factbook.extraction.util.WebHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements search engine client for the <a href="http://www.yahoo.com/">Yahoo</a> search engine.
 * See the <a href="https://developer.yahoo.com/boss/search/">description of API</a>
 */
@Component
public class YahooClient extends AbstractSearchEngineClient{

    @Value("${yahoo.client.key}")
    private String clientKey;

    @Value("${yahoo.client.secret}")
    private String clientSecret;

    @Override
    protected Logger log() {
        return LoggerFactory.getLogger(YahooClient.class);
    }

    @Override
    protected SearchEngine searchEngine() {
        return SearchEngine.YAHOO;
    }

    @Override
    protected int getMaxResultsPerPage() {
        return 50;
    }

    /**
     * Runs HTTP request using {@link WebHelper#getContentOAuth(String, String, String)} and parse results.
     *
     * @param request a query to the search engine
     * @return a list of links or empty list is nothing was found
     */
    protected List<Link> getLinks(Request request){
        List<Link> links = new ArrayList<>(getMaxResultsPerPage());
        try {
            String response = WebHelper.getContentOAuth(buildUrl(request), clientKey, clientSecret);
            JsonNode root = jsonMapper.readTree(response);
            JsonNode results = root.path("bossresponse").path("limitedweb").path("results");
            Iterator<JsonNode> itr = results.elements();
            while (itr.hasNext()) {
                JsonNode res = itr.next();
                links.add(new Link(res.path("url").textValue(),
                        WebHelper.removeTags(res.path("title").textValue()),
                        res.path("abstract").textValue(),
                        request.golem));
            }
        } catch (IOException e) {
            log().error("Error on request: {} ", request.query);
            log().error("Error info:", e);
        }
        return links;
    }

    /**
     * Builds Yahoo API request URL.
     *
     * @param request search query
     * @return HTTP request
     */
    private String buildUrl(Request request){
        // if we request not the first page add start parameter to URL
        String startParam = (request.start >= getMaxResultsPerPage()) ? "&start=" + (request.start + 1)  : "";
        return "https://yboss.yahooapis.com/ysearch/limitedweb?q=" + encodeQuery(request)
                + startParam;
                //"&view=" + query.golem.getMainLang().getCode().toLowerCase() +
                //"&key=" + clientKey +
                //"&format=json";
    }
}
