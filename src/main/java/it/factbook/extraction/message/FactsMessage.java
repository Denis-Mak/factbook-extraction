package it.factbook.extraction.message;

import it.factbook.search.Fact;

import java.util.List;

/**
 * Contains parsed facts.
 */
public class FactsMessage {
    private List<Fact> facts;

    /**
     *
     * @return a list of {@link Fact} parsed from the article
     */
    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }
}
