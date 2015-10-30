package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container of the information about one page we get from the search results of a search engine.
 */
public class Link {
    private static final Logger log = LoggerFactory.getLogger(Link.class);
    private String url;
    private String urlHash;
    private String title;
    private String snippet;
    private Golem golem;

    public Link(){}

    public Link (String url, String title, String snippet, Golem golem){
        this.url        = url;
        this.urlHash    = DigestUtils.sha1Hex(this.url);
        this.title      = title;
        this.snippet    = snippet;
        this.golem      = golem;
    }

    /**
     *
     * @return SHA1 hash of the link URL
     */
    public String getUrlHash() {
        return urlHash;
    }

    /**
     *
     * @return string representation of the link URL
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return title of the found page
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return brief text of the found page with found words
     */
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    /**
     *
     * @return dictionary that contains words from search query
     */
    public Golem getGolem() {
        return golem;
    }

    public void setGolem(Golem golem) {
        this.golem = golem;
    }
}
