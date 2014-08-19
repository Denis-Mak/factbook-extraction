package it.factbook.extraction;

import it.factbook.dictionary.Golem;

/**
 *
 */
public class DocumentMessage {
    private String title;
    private String url;
    private String content;
    private Golem golem;

    DocumentMessage(){

    }

    DocumentMessage(Link link, String content){
        this.content    = content;
        this.title      = link.getTitle();
        this.url        = link.getUrl();
        this.golem      = link.getGolem();
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Golem getGolem() {
        return golem;
    }

    public void setGolem(Golem golem) {
        this.golem = golem;
    }
}
