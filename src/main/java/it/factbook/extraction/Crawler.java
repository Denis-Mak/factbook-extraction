package it.factbook.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.util.WebHelper;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
@Component
public class Crawler implements MessageListener{
    private static final Logger log = LoggerFactory.getLogger(Crawler.class);

    @Autowired
    private CrawlerLog crawlerLog;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message) {
        SearchResultsMessage searchResultsMessage = new SearchResultsMessage();
        try {
            searchResultsMessage = jsonMapper.readValue(message.getBody(), SearchResultsMessage.class);
        } catch (IOException e) {
            log.error("Error during unpack SearchResultsMessage: {}", e);
        }
        log.debug("Received message. ProfileId: {}. Qty of links: {}.", searchResultsMessage.getProfileId(), searchResultsMessage.getLinks().size());
        for (Link link: searchResultsMessage.getLinks()){
            long startDownload = System.currentTimeMillis();
            String articleBody = "";
            try {
                articleBody = downloadArticle(link.getUrl());
                crawlerLog.logDownloadedArticles(searchResultsMessage.getProfileId(),
                        searchResultsMessage.getSearchEngine(),
                        searchResultsMessage.getRequestLogId(),
                        link.getUrl(),
                        articleBody,
                        startDownload,
                        "");
            } catch (Exception e) {
                log.error("Error get results: ", e);
                log.error("URL: {}", link.getUrl());
                crawlerLog.logDownloadedArticles(searchResultsMessage.getProfileId(),
                        searchResultsMessage.getSearchEngine(),
                        searchResultsMessage.getRequestLogId(),
                        link.getUrl(),
                        "",
                        startDownload,
                        e.getMessage());
            }
            if (articleBody.length() > 0) {
                DocumentMessage documentMessage = new DocumentMessage(link, articleBody);
                passDocumentToFactSaver(documentMessage);
            }
        }

    }

    private void passDocumentToFactSaver(DocumentMessage documentMessage) {
        try {
            String json = jsonMapper.writeValueAsString(documentMessage);
            amqpTemplate.convertAndSend(AmqpConfig.factSaverExchange().getName(), "#", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public String downloadArticle(String url) throws IOException, TikaException, SAXException {
        String content = "";
        InputStream is = null;
        try {
            Metadata metadata = new Metadata();
            is = WebHelper.getInputStream(url);
            String mimeType = new Tika().detect(is);
            switch (mimeType) {
                case "text/html":
                case "application/xhtml+xml": {
                    BodyContentHandler ch = new BodyContentHandler(100000); // Max symbols in document
                    BoilerpipeContentHandler bch = new BoilerpipeContentHandler(ch);
                    HtmlParser parser = new HtmlParser();
                    parser.parse(is, bch, metadata, new ParseContext());
                    content = ch.toString();
                    break;
                }
                case "application/pdf": {
                    BodyContentHandler ch = new BodyContentHandler();
                    PDFParser parser = new PDFParser();
                    parser.parse(is, ch, metadata, new ParseContext());
                    content = ch.toString();
                    break;
                }
            }
        } finally {
            if (is != null) is.close();
        }

        return content;
    }
}
