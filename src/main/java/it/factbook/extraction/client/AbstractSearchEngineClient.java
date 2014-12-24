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
import java.util.*;

/**
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

    protected abstract Logger log();

    protected abstract SearchEngine searchEngine();

    protected abstract int getMaxResultsPerPage();

    protected abstract List<Link> getLinks(Request request);

    @Override
    public void onMessage(Message message) {
        ProfileMessage profileMessage = unpackProfileMessage(message);
        long profileVersion = System.currentTimeMillis();
        List<Request> queries = getQueries(profileMessage);
        for(Request request : queries){
            Integer startRecord = checkPreviousRequests(crawlerLog.getRequests(request.query, searchEngine()));
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

    protected Integer checkPreviousRequests(List<Request> requests){
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

    protected List<Request> getQueries(ProfileMessage profileMessage) {
        String initialQuery = (profileMessage.getInitialQuery() != null) ? profileMessage.getInitialQuery() + " " : "";
        if (profileMessage.getQueryLines() != null && profileMessage.getQueryLines().size() > 0) {
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
            return Arrays.asList(request);
        } else {
            return Collections.<Request>emptyList();
        }

    }

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
}
