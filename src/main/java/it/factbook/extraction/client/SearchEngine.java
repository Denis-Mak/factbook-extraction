package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;

/**
 *
 */
public enum SearchEngine {
    FAROO(1, Golem.WIKI_EN),
    YANDEX(2, Golem.WIKI_RU),
    GOOGLE(3, Golem.WIKI_RU, Golem.WIKI_EN),
    YAHOO(4, Golem.WIKI_RU, Golem.WIKI_EN),
    BING(5, Golem.WIKI_RU, Golem.WIKI_EN);

    private final int id;
    private final Golem[] golems;

    SearchEngine(int id, Golem... golems){
        this.id = id;
        this.golems = golems;
    }

    public int getId() {return id;}

    public Golem[] getGolemIds() {return golems;}

    public boolean containsGolem (Golem golem){
        for (Golem elm: golems){
            if (elm == golem) {return true;}
        }
        return false;
    }
}
