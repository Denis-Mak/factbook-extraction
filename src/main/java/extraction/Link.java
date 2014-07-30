package extraction;

import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 */
public class Link {
    private String url;
    private String urlHash;
    private String title;
    private String snippet;
    private int golemId;

    public Link (String url, String title, String snippet, int golemId){
        this.urlHash    = DigestUtils.sha1Hex(url);
        this.url        = url;
        this.title      = title;
        this.snippet    = snippet;
        this.golemId    = golemId;
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
}
