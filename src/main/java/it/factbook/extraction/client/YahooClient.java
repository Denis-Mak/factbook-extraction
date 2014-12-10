package it.factbook.extraction.client;

import com.fasterxml.jackson.databind.JsonNode;
import it.factbook.dictionary.Golem;
import it.factbook.dictionary.WordForm;
import it.factbook.extraction.Link;
import it.factbook.extraction.message.ProfileMessage;
import it.factbook.extraction.util.WebHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 *
 */
@Component
public class YahooClient extends AbstractSearchEngineClient implements SearchEngineClient {
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
            sendToCrawler(profileMessage.getProfileId(), SEARCH_ENGINE, requestLogId, linksToCrawl);
        }
    }

    @Override
    public List<Query> getQueries(ProfileMessage profileMessage) {
        int WORDGRAMS_IN_ONE_QUERY = 5;
        List<Query> wordgramQueries = new ArrayList<>();
        String initialQuery = (profileMessage.getInitialQuery() == null || profileMessage.getInitialQuery().length() < 1) ? "" : profileMessage.getInitialQuery();
        if (profileMessage.getQueryLines() != null && profileMessage.getQueryLines().size() > 0) {
            String tmp = "";
            for(String word: initialQuery.split("\\s")){
                tmp += "+" + word + " ";
            }
            initialQuery = tmp;
            StringJoiner sj = new StringJoiner(" ", initialQuery, " OR ");
            for (List<List<WordForm>> line: profileMessage.getQueryLines()){
                for(List<WordForm> wordgram: line){
                    for (WordForm word : wordgram) {
                        sj.add(word.getWord());
                    }
                    if (wordgram.get(0).getGolem() != Golem.UNKNOWN) {
                        wordgramQueries.add(new Query(wordgram.get(0).getGolem(), sj.toString()));
                    }
                    sj = new StringJoiner(" ", initialQuery, " OR ");
                }
            }
            Collections.sort(wordgramQueries, (q1, q2) -> q1.golem.getId() - q2.golem.getId());
            List<Query> queries = new ArrayList<>();
            int wordgramCounter = 0;
            Golem golem = Golem.UNKNOWN;

            String queryStr = "";
            for (Query each:wordgramQueries){
                if (wordgramCounter == 0) golem = each.golem;
                if (each.golem != golem || wordgramCounter >= WORDGRAMS_IN_ONE_QUERY){
                    wordgramCounter = 0;
                    queries.add(new Query(golem, queryStr.substring(0, queryStr.length()-4)));
                    queryStr = "";
                    golem = each.golem;
                }

                wordgramCounter++;
                queryStr += each.query;
            }
            queries.add(new Query(golem, queryStr.substring(0, queryStr.length()-4)));

            return queries;
        } else if (!"".equals(initialQuery)) {
            Query query = new Query(predictGolem(initialQuery), initialQuery);
            return Arrays.asList(query);
        } else {
            return Collections.<Query>emptyList();
        }
    }

    List<Link> getLinks(Query query){
        List<Link> links = new ArrayList<>(50);
        try {
            String response = WebHelper.getContentOAuth(buildUrl(query), clientKey, clientSecret);
            JsonNode root = jsonMapper.readTree(response);
            JsonNode results = root.path("bossresponse").path("limitedweb").path("results");
            Iterator<JsonNode> itr = results.elements();
            while (itr.hasNext()) {
                JsonNode res = itr.next();
                links.add(new Link(res.path("url").textValue(),
                        WebHelper.removeTags(res.path("title").textValue()),
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
            encQuery = URLEncoder.encode(query.query, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported Encoding!");
        }

        return "https://yboss.yahooapis.com/ysearch/limitedweb?q=" + encQuery;
                //"&view=" + query.golem.getMainLang().getCode().toLowerCase() +
                //"&key=" + clientKey +
                //"&format=json";
    }
}
