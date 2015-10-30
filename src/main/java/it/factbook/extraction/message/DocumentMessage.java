package it.factbook.extraction.message;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.search.DocType;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.joda.time.DateTime;

/**
 * Contains page article and its properties
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

    /**
     *
     * @return title of the page
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return url of the page as a string
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return main article of the page
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     *
     * @return dictionary to parse the article
     */
    public Golem getGolem() {
        return golem;
    }

    public void setGolem(Golem golem) {
        this.golem = golem;
    }

    /**
     *
     * @return published date of the article
     */
    public DateTime getPublished() {
        return published;
    }

    public void setPublished(DateTime published) {
        this.published = published;
    }

    /**
     *
     * @return document type of the article as {@link DocType} value
     */
    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }
}
