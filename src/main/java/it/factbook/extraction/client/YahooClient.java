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

    private String buildUrl(Request request){
        String encQuery = null;
        try {
            encQuery = URLEncoder.encode(request.query, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            log().error("Unsupported Encoding!");
        }
        // if we request not the first page add start parameter to URL
        String startParam = (request.start >= getMaxResultsPerPage()) ? "&start=" + (request.start + 1)  : "";
        return "https://yboss.yahooapis.com/ysearch/limitedweb?q=" + encQuery
                + startParam;
                //"&view=" + query.golem.getMainLang().getCode().toLowerCase() +
                //"&key=" + clientKey +
                //"&format=json";
    }
}
