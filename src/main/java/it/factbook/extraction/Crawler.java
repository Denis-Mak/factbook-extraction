package it.factbook.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.message.DocumentMessage;
import it.factbook.extraction.message.SearchResultsMessage;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            log.error("Error during unpack SearchResultsMessage: ", e);
        }
        log.debug("Received message. ProfileId: {}. Qty of links: {}.", searchResultsMessage.getProfileId(), searchResultsMessage.getLinks().size());
        final long requestLogId = searchResultsMessage.getRequestLogId();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (Link link: searchResultsMessage.getLinks()){
            executor.execute(() -> {
                long startDownload = System.currentTimeMillis();
                String content = "";
                try {
                    log.debug("Start download link: {}", link.getUrl());
                    content = downloadArticle(link.getUrl());
                    crawlerLog.logDownloadedArticles(requestLogId,
                            link.getUrl(),
                            content,
                            startDownload,
                            "");
                    log.debug("Link downloaded: {}", link.getUrl());
                } catch (Exception e) {
                    log.error("Error get results: ", e);
                    log.error("URL: {}", link.getUrl());
                    crawlerLog.logDownloadedArticles(requestLogId,
                            link.getUrl(),
                            "",
                            startDownload,
                            e.getMessage());
                }
                if (content.length() > 0) {
                    if (link.getTitle() == null) {
                        link.setTitle("");
                    }
                    DocumentMessage documentMessage = new DocumentMessage(link, content);
                    passDocumentToFactSaver(documentMessage);
                }
            });
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

    protected String downloadArticle(String url) throws IOException, TikaException, SAXException {
        InputStream is = null;
        String content = "";
        try {
            Metadata metadata = new Metadata();
            is = WebHelper.getInputStream(url);
            String mimeType = new Tika().detect(is);
            switch (mimeType) {
                case "text/html":
                case "application/xhtml+xml": {
                    BodyContentHandler ch = new BodyContentHandler(100000); // Max symbols in document
                    BoilerpipeContentHandler bch = new BoilerpipeContentHandler(ch, CommonExtractors.ARTICLE_EXTRACTOR);
                    HtmlParser parser = new HtmlParser();
                    parser.parse(is, bch, metadata, new ParseContext());
                    content = ch.toString();
                    break;
                }
                case "application/pdf": {
                    BodyContentHandler ch = new BodyContentHandler();
                    PDFParser parser = new PDFParser();
                    parser.parse(is, ch, metadata, new ParseContext());
                    content = ch.toString().replace("-\n\n", "").replace("-\n", "").replace("\n"," ");
                    break;
                }
            }
        } finally {
            if (is != null) is.close();
        }

        return content;
    }

    protected String getTitle(Metadata metadata, String content){
        String title = metadata.get("title");
        if (title == null){
            title = metadata.get("");
        }
        if (title == null) {
            int newLine = content.indexOf(System.lineSeparator());
            int fullStop = content.indexOf('.');
            if (newLine > -1 && newLine < fullStop) {
                title = content.substring(0, newLine);
            } else if (fullStop > -1 && fullStop < newLine) {
                title = content.substring(0, fullStop);
            } else {
                title = content.substring(0, 100) + "...";
            }
        }
        return title;
    }
}
