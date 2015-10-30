package it.factbook.extraction.message;

import it.factbook.dictionary.Golem;

/**
 * Contains a fact to parse and dictionary using to parse the fact.
 */
public class TreeBuilderMessage {
    private Golem golem;
    private String text;

    public Golem getGolem() {
        return golem;
    }

    public String getText() {
        return text;
    }
}
