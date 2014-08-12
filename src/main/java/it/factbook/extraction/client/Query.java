package it.factbook.extraction.client;

/**
 *
 */
public class Query {
    int golemId;
    String query;

    public Query(int golemId, String query){
        this.golemId    = golemId;
        this.query      = query;
    }
}
