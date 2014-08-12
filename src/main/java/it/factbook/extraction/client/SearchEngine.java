package it.factbook.extraction.client;

/**
 *
 */
public enum SearchEngine {
    FAROO(1, new int[]{2}),
    YANDEX(2, new int[]{1}),
    GOOGLE(3, new int[]{1,2}),
    YAHOO(4, new int[]{1,2}),
    BING(5, new int[]{1,2});

    private final int id;
    private final int[] golemIds;

    SearchEngine(int id, int[] golemId){
        this.id = id;
        this.golemIds = golemId;
    }

    public int getId() {return id;}

    public int[] getGolemIds() {return golemIds;}

    public boolean containsGolemId (int golemId){
        for (int elm: golemIds){
            if (elm == golemId) {return true;}
        }
        return false;
    }
}
