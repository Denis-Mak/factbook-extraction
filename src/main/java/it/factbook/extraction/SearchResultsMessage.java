package it.factbook.extraction;

import it.factbook.extraction.client.SearchEngine;

import java.util.List;

/**
 *
 */
public class SearchResultsMessage {
    private long profileId;
    private SearchEngine searchEngine;
    private long profileVersion;
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

    public long getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(long profileVersion) {
        this.profileVersion = profileVersion;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
