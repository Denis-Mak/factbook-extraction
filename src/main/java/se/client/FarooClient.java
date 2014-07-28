package se.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import extraction.Link;
import extraction.ProfileMessage;
import extraction.SearchResultsMessage;
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
    private static final Logger log = LoggerFactory.getLogger(FarooClient.class);
    private static ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public void onMessage(Message message) {
        try {
            List<String> queries = getQueries(jsonMapper.readValue(message.getBody(), ProfileMessage.class));
            queries.stream().forEach(q -> passResultsToCrawler(getLinks(q)));
        } catch (IOException e) {
            log.error("Error during unpack ProfileMessage: {}", e);
        }
    }

    List<String> getQueries(ProfileMessage msg){
        List<String> queries = new ArrayList<>(50);
        for (List<List<String>> line: msg.getQueryLines()){
            for(List<String> wordgram: line){
                String query = msg.getInitialQuery() != null ? msg.getInitialQuery() : "";
                for (String word: wordgram){
                    query += " " + word;
                }
                queries.add(query);
            }
        }
        return queries;
    }

    List<Link> getLinks(String query){
        List<Link> links = new ArrayList<>(100);
        try {
            JsonNode root = jsonMapper.readTree(WebHelper.getUrl(buildUrl(query), "factbook-robot"));
            JsonNode results = root.path("results");
            Iterator<JsonNode> itr = results.elements();
            while (itr.hasNext()) {
                JsonNode res = itr.next();
                links.add(new Link( res.path("url").textValue(),
                                    res.path("title").textValue(),
                                    res.path("kwic").textValue()));
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

    public void passResultsToCrawler(List<Link> results){
        SearchResultsMessage msg = new SearchResultsMessage(results);
        amqpTemplate.convertAndSend("crawler-query", msg);
    }
}
