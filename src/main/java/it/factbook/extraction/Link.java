package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 *
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
        try {
            this.url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Exception in URL encoding, URL: {}", url, e);
        }
        this.urlHash    = DigestUtils.sha1Hex(this.url);
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
        try {
            this.url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Exception in URL encoding, URL: {}", url, e);
        }
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
