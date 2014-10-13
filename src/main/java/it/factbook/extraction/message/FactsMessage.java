package it.factbook.extraction.message;

import it.factbook.search.Fact;

import java.util.List;

/**
 *
 */
public class FactsMessage {
    private List<Fact> facts;

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }
}
