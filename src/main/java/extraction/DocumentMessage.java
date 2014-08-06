package extraction;

/**
 *
 */
public class DocumentMessage {
    private String title;
    private String url;
    private String content;
    private int golemId;

    DocumentMessage(){

    }

    DocumentMessage(Link link, String content){
        this.content    = content;
        this.title      = link.getTitle();
        this.url        = link.getUrl();
        this.golemId    = link.getGolemId();
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

    public int getGolemId() {
        return golemId;
    }

    public void setGolemId(int golemId) {
        this.golemId = golemId;
    }
}
