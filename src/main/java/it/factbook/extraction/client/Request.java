package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import org.joda.time.DateTime;

/**
 * Container of a request to a search engine with additional properties.
 */
public class Request {
    /**
     * Dictionary used to parse the profile line we have taken query from
     */
    public final Golem golem;

    /**
     * Query to search
     */
    public final String query;

    /**
     * Requested search result from this line
     */
    public final int start;

    /**
     * Timestamp of the request
     */
    public final DateTime requested;

    public Request(Golem golem, String query, int start, DateTime requested){
        this.golem      = golem;
        this.query      = query;
        this.start      = start;
        this.requested  = requested;
    }
}
