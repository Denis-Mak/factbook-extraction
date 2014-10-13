package it.factbook.extraction.message;

import it.factbook.extraction.Link;
import it.factbook.extraction.client.SearchEngine;

import java.util.List;

/**
 *
 */
public class SearchResultsMessage {
    private long profileId;
    private SearchEngine searchEngine;
    private long requestLogId;
    private List<Link> links;

    public  SearchResultsMessage(){}

    public SearchResultsMessage(List<Link> links){
        this.links = links;
    }

    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    public long getRequestLogId() {
        return requestLogId;
    }

    public void setRequestLogId(long profileVersion) {
        this.requestLogId = profileVersion;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
