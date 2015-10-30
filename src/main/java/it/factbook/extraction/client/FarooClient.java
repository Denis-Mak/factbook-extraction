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
 * Implements search engine client for the <a href="http://www.faroo.com/">Faroo</a> search engine.
 * See the <a href="http://www.faroo.com/hp/api/api.html">description of API</a>
 */
@Component
public class FarooClient extends AbstractSearchEngineClient{

    @Value("${faroo.client.api.key}")
    private String appKey;

    @Override
    protected Logger log() {
        return LoggerFactory.getLogger(FarooClient.class);
    }

    @Override
    protected SearchEngine searchEngine() {
        return SearchEngine.FAROO;
    }

    @Override
    protected int getMaxResultsPerPage() {
        return 10;
    }

    private static long lastAccessed = 0;

    /**
     * Runs HTTP request using {@link WebHelper#getContent(String)} and parse results.
     *
     * @param request a query to the search engine
     * @return a list of links or empty list is nothing was found
     */
    @Override
    protected List<Link> getLinks(Request request){
        List<Link> links = new ArrayList<>(getMaxResultsPerPage());
        try {
            pauseBetweenRequests();
            JsonNode root = jsonMapper.readTree(WebHelper.getContent(buildUrl(request)));
            JsonNode results = root.path("results");
            Iterator<JsonNode> itr = results.elements();
            while (itr.hasNext()) {
                JsonNode res = itr.next();
                links.add(new Link(res.path("url").textValue(),
                        res.path("title").textValue(),
                        res.path("kwic").textValue(),
                        request.golem));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return links;
    }

    /**
     * Builds Faroo API request URL.
     *
     * @param request search query
     * @return HTTP request
     */
    private String buildUrl(Request request){
        // if we are requesting not the first page add start parameter to URL
        String startParam = (request.start >= getMaxResultsPerPage()) ? "&start=" + (request.start + 1)  : "";
        return "http://www.faroo.com/api?q=" + encodeQuery(request)
                + startParam
                + "&length=10&l=en&src=news&i=false&f=json&key=" + appKey;
    }

    /**
     * Timer to make a delay between two requests to satisfy API requirements.
     */
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
