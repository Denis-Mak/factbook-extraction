package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;

/**
 *
 */
public class Query {
    public Golem golem;
    public String query;

    public Query(Golem golem, String query){
        this.golem      = golem;
        this.query      = query;
    }
}
