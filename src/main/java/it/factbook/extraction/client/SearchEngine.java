package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;

import java.util.HashMap;
import java.util.Map;

/**
 * List of all available search engines.
 */
public enum SearchEngine {
    FAROO(1, Golem.WIKI_EN),
    //YANDEX(2, Golem.WIKI_RU),
    GOOGLE(3, Golem.WIKI_RU, Golem.WIKI_EN),
    YAHOO(4, Golem.WIKI_RU, Golem.WIKI_EN),
    BING(5, Golem.WIKI_RU, Golem.WIKI_EN);

    /**
     * Identificator of the search engine in database
     */
    private final int id;

    /**
     * Supported dictionaries
     */
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

    /**
     * Returns ID of the search engine
     *
     * @return unique ID for DB records
     */
    public int getId() {return id;}

    /**
     * Checks if the provided dictionary supported.
     *
     * @param golem dictionary to check
     * @return true if supported
     */
    public boolean containsGolem (Golem golem){
        for (Golem elm: golems){
            if (elm == golem) {return true;}
        }
        return false;
    }

    /**
     * Returns this search engine by its ID.
     *
     * @param searchEngineId ID
     * @return this value
     */
    public static SearchEngine valueOf(int searchEngineId){
        return map.get(searchEngineId);
    }
}
