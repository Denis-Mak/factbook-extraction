package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 */
public class Link {
    private String url;
    private String urlHash;
    private String title;
    private String snippet;
    private Golem golem;

    public Link(){}

    public Link (String url, String title, String snippet, Golem golem){
        this.urlHash    = DigestUtils.sha1Hex(url);
        this.url        = url;
        this.title      = title;
        this.snippet    = snippet;
        this.golem      = golem;
    }

    public String getUrlHash() {
        return urlHash;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public Golem getGolem() {
        return golem;
    }

    public void setGolem(Golem golem) {
        this.golem = golem;
    }
}
