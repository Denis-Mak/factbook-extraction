package it.factbook.extraction.client;

import com.fasterxml.jackson.databind.JsonNode;
import it.factbook.extraction.Link;
import it.factbook.extraction.util.WebHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class BingClient extends AbstractSearchEngineClient {

    @Value("${bing.client.api.key}")
    private String appKey;

    @Override
    protected Logger log() {
        return LoggerFactory.getLogger(BingClient.class);
    }

    @Override
    protected SearchEngine searchEngine() {
        return SearchEngine.BING;
    }

    @Override
    protected int getMaxResultsPerPage() {
        return 50;
    }

    @Override
    protected List<Link> getLinks(Request request){
        List<Link> links = new ArrayList<>(getMaxResultsPerPage());
        try {
            JsonNode root = jsonMapper.readTree(WebHelper.getContent(buildUrl(request), null, appKey, appKey));
            JsonNode results = root.path("d").path("results").get(0).path("Web");
            Iterator<JsonNode> itr = results.elements();
            while (itr.hasNext()) {
                JsonNode res = itr.next();
                links.add(new Link(res.path("Url").textValue(),
                        res.path("Title").textValue(),
                        res.path("Description").textValue(),
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
            encQuery = URLEncoder.encode(request.query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log().error("Unsupported Encoding!");
        }
        // if we request not the first page add start parameter to URL
        String startParam = (request.start >= getMaxResultsPerPage()) ? "&$skip=" + (request.start + 1)  : "";
        return "https://api.datamarket.azure.com/Bing/Search/Composite?"

                // Common request fields (required)
                + "Query=%27" + encQuery + "+language:" + request.golem.getMainLang().getCode().toLowerCase() + "%27"
                + "&Sources=%27web%27"

                // Web-specific request fields (optional)
                //+ "&$top=10"
                + startParam
                //+ "&Options=%27DisableLocationDetection%2BEnableHighlighting%27 "

                // JSON-specific request fields (optional)
                + "&$format=json";
    }
}
