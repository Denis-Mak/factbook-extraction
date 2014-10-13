package it.factbook.extraction.message;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;

/**
 *
 */
public class DocumentMessage {
    private String title;
    private String url;
    private String content;
    private Golem golem;

    public DocumentMessage(){

    }

    public DocumentMessage(Link link, String content){
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
