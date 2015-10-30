package it.factbook.extraction.message;

import it.factbook.extraction.Link;
import it.factbook.extraction.client.SearchEngine;

import java.util.List;

/**
 * Contains links that was received from a search engine by a search engine client.
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

    /**
     *
     * @return ID of the profile that is request external search
     */
    public long getProfileId() {
        return profileId;
    }

    /**
     * Profile ID is used to log statistic of search requests and find source of the request
     *
     * @param profileId any long greater than 0
     */
    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }

    /**
     *
     * @return search engine that was found these links as a value of {@link SearchEngine}
     */
    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    /**
     * Reference to search engine one of {@link SearchEngine} used to keep statistic of returned links
     *
     * @param searchEngine a value of {@link SearchEngine}
     */
    public void setSearchEngine(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    /**
     *
     *
     * @return ID of search engine request
     */
    public long getRequestLogId() {
        return requestLogId;
    }

    /**
     * Surrogate key of a request to search engine record in the RequestLog table, to track link between search request
     * and downloaded link
     *
     * @param profileVersion ID of search engine request
     */
    public void setRequestLogId(long profileVersion) {
        this.requestLogId = profileVersion;
    }

    /**
     *
     * @return a list of links to download
     */
    public List<Link> getLinks() {
        return links;
    }

    /**
     * A List of links to download.
     *
     * @param links unique links to download, downloading does in order of the links
     */
    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
