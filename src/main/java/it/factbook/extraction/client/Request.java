package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import org.joda.time.DateTime;

/**
 *
 */
public class Request {
    public final Golem golem;
    public final String query;
    public final int start;
    public final DateTime requested;

    public Request(Golem golem, String query, int start, DateTime requested){
        this.golem      = golem;
        this.query      = query;
        this.start      = start;
        this.requested  = requested;
    }
}
