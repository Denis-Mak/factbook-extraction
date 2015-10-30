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
 *
 */
@Component
public class GoogleClient extends AbstractSearchEngineClient{

    @Value("${google.client.user.id}")
    private String userId;

    @Value("${google.client.search.engine.id}")
    private String engineId;

    @Value("${google.client.api.key}")
    private String appKey;

    @Override
    protected Logger log() {
        return LoggerFactory.getLogger(GoogleClient.class);
    }

    @Override
    protected SearchEngine searchEngine() {
        return SearchEngine.GOOGLE;
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
            String response = WebHelper.getContent(buildUrl(request));
            JsonNode results = jsonMapper.readTree(response).path("items");
            Iterator<JsonNode> itr = results.elements();
            while (itr.hasNext()) {
                JsonNode res = itr.next();
                links.add(new Link(res.path("link").textValue(),
                        res.path("title").textValue(),
                        res.path("snippet").textValue(),
                        request.golem));
            }
        } catch (IOException e) {
            log().error("Error on request: {} ", request.query);
            log().error("Error info:", e);
        }
        return links;
    }

    /**
     * Builds Google API request URL.
     *
     * @param request search query
     * @return HTTP request
     */
    private String buildUrl(Request request){
        // if we request not the first page add start parameter to URL
        String startParam = (request.start >= getMaxResultsPerPage()) ? "&start=" + (request.start + 1)  : "";
        return "https://www.googleapis.com/customsearch/v1?key=" + appKey +
                "&lr=lang_" + request.golem.mainLang().getCode().toLowerCase() +
                "&cx=" + userId + ":" + engineId +
                "&q=" + encodeQuery(request) +
                startParam;
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
