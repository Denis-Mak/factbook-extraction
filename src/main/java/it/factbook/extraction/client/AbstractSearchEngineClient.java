package it.factbook.extraction.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.factbook.dictionary.Golem;
import it.factbook.dictionary.LangDetector;
import it.factbook.dictionary.WordForm;
import it.factbook.extraction.CrawlerLog;
import it.factbook.extraction.Link;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.message.ProfileMessage;
import it.factbook.extraction.message.SearchResultsMessage;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.slf4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Implements common methods of all search engine clients.
 * Typical sequence of a client job:
 * <ul>
 *     <li>receive message with search profile</li>
 *     <li>build queries for the search engine from profile</li>
 *     <li>run queries </li>
 *     <li>parse results and pack into a list of links</li>
 *     <li>send message to crawler</li>
 * </ul>
 *
 * The default implementation of query builder using basic space separated text
 * supported by all search engines.
 * The inherited classes must implements three simple methods to provide information about the search engine
 * ({@link #log()}, {@link #getMaxResultsPerPage()}, {@link #searchEngine()})
 *
 * And main method {@link #getLinks(Request)} to send the request and parse results.
 *
 */
@Component
public abstract class AbstractSearchEngineClient implements MessageListener {
    private static final int SEARCH_MAX_DEPTH_PAGES = 3;

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    CrawlerLog crawlerLog;

    @Autowired
    LangDetector langDetector;


    static ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Returns logger of the inherited class
     *
     * @return an instance of {@link Logger} configured to the particular class
     */
    protected abstract Logger log();

    /**
     * Returns search engine indicator of the particular client implementation
     *
     * @return a value of {@link SearchEngine}
     */
    protected abstract SearchEngine searchEngine();

    /**
     * Returns maximum results per page limit for the particular engine
     *
     * @return integer from 0 to MAX_INT
     */
    protected abstract int getMaxResultsPerPage();

    /**
     * Runs the provided request, parses the results and return a list of {@link Link} that contains URL,
     * title and other properties of found page.
     *
     * @param request a query to the search engine
     * @return the list of {@link Link} or empty list if nothing was found
     */
    protected abstract List<Link> getLinks(Request request);

    /**
     * Receives incoming message, and runs all methods of the processing sequence.
     * It is like controller of a search engine client.
     *
     * @param message an instance of {@link ProfileMessage}
     */
    @Override
    public void onMessage(Message message) {
        ProfileMessage profileMessage = unpackProfileMessage(message);
        long profileVersion = System.currentTimeMillis();
        List<Request> requests = getQueries(profileMessage);
        for(Request request : requests){
            //get all previous requests for this query and find if we need to get search results and what page of the results
            Integer startRecord = getStartRecordNm(crawlerLog.getRequests(request.query, searchEngine()));
            if (startRecord != null) {
                // set start record field
                request = new Request(request.golem, request.query, startRecord, request.requested);
                long requestLogId = crawlerLog.logSearchRequest(profileMessage.getProfileId(), searchEngine(), profileVersion, request);
                List<Link> foundLinks = getLinks(request);
                List<Link> linksToCrawl = crawlerLog.getLinksToCrawl(foundLinks);
                crawlerLog.logReturnedResults(requestLogId, foundLinks.size(), linksToCrawl.size());
                sendToCrawler(profileMessage.getProfileId(), searchEngine(), requestLogId, linksToCrawl);
            } else {
                crawlerLog.incrementCashHits(request.query, searchEngine());
            }
        }
    }

    /**
     * Returns the first record of the search result to ask from search engine.
     * Checks if previous query not older then a month. If this request
     * is absolutely new - returns 0 (the beginning of search results),
     * if we have parsed maximum depth of results for this
     * request - returns null.
     *
     * @param requests all previous requests for this query
     * @return index of the first record to request
     */
    protected Integer getStartRecordNm(List<Request> requests){
        // If there was no previous requests, query the first page of search results
        if (requests == null || requests.size() < 1){
            return 0;
        }
        Collections.sort(requests, (r1,r2) -> Integer.compare(r1.start, r2.start));
        // check if previous request is stale
        if (Months.monthsBetween(requests.get(0).requested, new DateTime()).getMonths() > 1){
            return 0;
        }
        // if we have not read depth enough search results return new start record
        int prevStartRecord = requests.get(requests.size()-1).start;
        if (prevStartRecord < SEARCH_MAX_DEPTH_PAGES * getMaxResultsPerPage()) {
            return prevStartRecord + getMaxResultsPerPage();
        }
        // if we have already have all results for this query and these results are up to date
        return null;
    }

    /**
     * Extract {@link ProfileMessage} from {@link Message}
     *
     * @param message received message from message queue, an instance implemented {@link Message} interface
     * @return unpacked instance of {@link ProfileMessage}
     */
    private ProfileMessage unpackProfileMessage(Message message){
        ProfileMessage profileMessage = null;
        try {
            profileMessage = jsonMapper.readValue(message.getBody(), ProfileMessage.class);
            log().debug("Received message. Profile ID: {}, {} lines.", profileMessage.getProfileId(), profileMessage.getQueryLines().size());
        } catch (IOException e) {
            log().error("Error during unpack ProfileMessage: {}", e);
        }
        return profileMessage;
    }

    /**
     * Creates and send a message to the Crawler. All processed link and profile metadata packed to this message.
     *
     * @param profileId ID of the profile that initiated the search
     * @param searchEngine search engine that returned results
     * @param requestLogId surrogate key of this request for searching
     * @param linksToCrawl a list of links extracted from the search results
     */
    protected void sendToCrawler(long profileId, SearchEngine searchEngine, long requestLogId, List<Link> linksToCrawl){
        if (linksToCrawl == null || linksToCrawl.size() < 1) {
            return;
        }
        crawlerLog.logFoundLinks(profileId, searchEngine, requestLogId, linksToCrawl);
        SearchResultsMessage searchResultsMessage = new SearchResultsMessage(linksToCrawl);
        searchResultsMessage.setProfileId(profileId);
        searchResultsMessage.setSearchEngine(searchEngine);
        searchResultsMessage.setRequestLogId(requestLogId);
        try {
            String json = jsonMapper.writeValueAsString(searchResultsMessage);
            amqpTemplate.convertAndSend(AmqpConfig.crawlerExchange().getName(), "#", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build a list of queries on the basis of the specified profile. In the default implementation each line
     * forms a query to a search engine. Format of the query: user entered words + all idioms from the profile line
     * (separated by space)
     *
     * @param profileMessage message contains search profile
     * @return the list of queries to a search engine
     */
    protected List<Request> getQueries(ProfileMessage profileMessage) {
        String initialQuery = (profileMessage.getInitialQuery() != null) ? profileMessage.getInitialQuery() + " " : "";
        if (!profileMessage.getQueryLines().isEmpty()) {
            List<Request> wordgramQueries = new ArrayList<>();
            StringJoiner sj = new StringJoiner(" ", initialQuery, "");
            for (List<List<WordForm>> line: profileMessage.getQueryLines()){
                for(List<WordForm> wordgram: line){
                    if (searchEngine().containsGolem(wordgram.get(0).getGolem())) {
                        for (WordForm word : wordgram) {
                            sj.add(word.getWord());
                        }
                        wordgramQueries.add(new Request(wordgram.get(0).getGolem(), sj.toString(), 0, new DateTime()));
                        sj = new StringJoiner(" ", initialQuery, "");
                    }
                }
            }
            return wordgramQueries;
        } else if (!"".equals(initialQuery)) {
            Request request = new Request(predictGolem(initialQuery), initialQuery.trim(), 0, new DateTime());
            return Collections.singletonList(request);
        } else {
            return Collections.<Request>emptyList();
        }

    }

    /**
     * Returns the Golem based on text language.
     *
     * @param str text to analise
     * @return a value of {@link Golem}
     */
    protected Golem predictGolem(String str){
        switch (langDetector.detectLanguage(str)){
            case RU:
                return Golem.WIKI_RU;
            case EN:
                return Golem.WIKI_EN;
            default:
                return Golem.UNKNOWN;
        }
    }

    /**
     * Encodes the query
     *
     * @param request a request to a search engine
     * @return encoded query from the request
     */
    protected String encodeQuery(final Request request){
        String encQuery = null;
        try {
            encQuery = URLEncoder.encode(request.query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log().error("Unsupported Encoding!");
        }
        return encQuery;
    }
}
