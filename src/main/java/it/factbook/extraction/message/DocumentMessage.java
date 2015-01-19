package it.factbook.extraction.message;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.search.DocType;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.joda.time.DateTime;

/**
 *
 */
public class DocumentMessage {
    private String title;
    private String url;
    private String content;
    private DocType docType;
    private Golem golem;
    private DateTime published;

    public DocumentMessage(){

    }

    public DocumentMessage(Link link, String content, Metadata metadata){
        this.content    = content;
        String metadataTitle = metadata.get(TikaCoreProperties.TITLE);
        String linkTitle = link.getTitle();
        if (metadataTitle != null && metadataTitle.length() > linkTitle.length()){
            this.title = metadataTitle;
        } else {
            this.title = linkTitle;
        }
        this.url        = link.getUrl();
        this.golem      = link.getGolem();
        this.published  = new DateTime(metadata.get(TikaCoreProperties.CREATED));
        String mimeType = metadata.get("Content-Type");
        if (mimeType != null) {
            if (mimeType.startsWith("text/html") || mimeType.startsWith("application/xhtml+xml")){
                this.docType = DocType.HTML;
            } else if (mimeType.startsWith("application/pdf")) {
                this.docType = DocType.PDF;
            }
        }
        if (docType == null) {
            this.docType = DocType.UNKNOWN;
        }
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

    public DateTime getPublished() {
        return published;
    }

    public void setPublished(DateTime published) {
        this.published = published;
    }

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }
}
