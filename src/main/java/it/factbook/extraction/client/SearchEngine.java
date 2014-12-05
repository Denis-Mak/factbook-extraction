package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;

import java.util.HashMap;
import java.util.Map;

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

    private static Map<Integer, SearchEngine> map = new HashMap<>();

    static {
        for (SearchEngine se : SearchEngine.values()) {
            map.put(se.id, se);
        }
    }

    public int getId() {return id;}

    public Golem[] getGolemIds() {return golems;}

    public boolean containsGolem (Golem golem){
        for (Golem elm: golems){
            if (elm == golem) {return true;}
        }
        return false;
    }

    public static SearchEngine valueOf(int searchEngineId){
        return map.get(searchEngineId);
    }
}
