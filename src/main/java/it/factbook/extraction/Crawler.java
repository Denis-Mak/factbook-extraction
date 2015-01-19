package it.factbook.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import fr.limsi.dctfinder.DCTExtractor;
import fr.limsi.dctfinder.DCTExtractorException;
import fr.limsi.dctfinder.PageInfo;
import fr.limsi.tools.common.LanguageTools;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.message.DocumentMessage;
import it.factbook.extraction.message.SearchResultsMessage;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
@Component
public class Crawler implements MessageListener{
    private static final Logger log = LoggerFactory.getLogger(Crawler.class);
    private static final int READ_TIMEOUT = 30000;
    private static final int CONNECTION_TIMEOUT = 1500;

    @Autowired
    private CrawlerLog crawlerLog;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${wapiti.binary}")
    private String wapitiBinaryPath;

    private static ObjectMapper jsonMapper = new ObjectMapper();
    static {
        jsonMapper.registerModule(new JodaModule());
    }

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
                Metadata metadata = new Metadata();
                try {
                    log.debug("Start download link: {}", link.getUrl());
                    content = parseArticle(link.getUrl(), metadata);
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
                    DocumentMessage documentMessage = new DocumentMessage(link, content, metadata);
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

    protected String parseArticle(String urlStr, Metadata metadata) throws IOException, TikaException, SAXException, DCTExtractorException {
        String content = "";
        URL url = new URL(urlStr);
        try(InputStream is = TikaInputStream.get(getConnection(url).getInputStream())) {
            Tika tika = new Tika();
            String mimeType = tika.detect(is);
            switch (mimeType) {
                case "text/html":
                case "application/xhtml+xml": {
                    BodyContentHandler ch = new BodyContentHandler(100000); // Max symbols in document
                    BoilerpipeContentHandler bch = new BoilerpipeContentHandler(ch, CommonExtractors.ARTICLE_EXTRACTOR);
                    HtmlParser parser = new HtmlParser();
                    parser.parse(is, bch, metadata, new ParseContext());
                    content = ch.toString();
                    getTitleAndPublished(urlStr, metadata);
                    break;
                }
                case "application/pdf": {
                    BodyContentHandler ch = new BodyContentHandler();
                    PDFParser parser = new PDFParser();
                    parser.parse(is, ch, metadata, new ParseContext());
                    content = ch.toString().replace("-\n\n", "").replace("-\n", "").replace("\n", " ");
                    break;
                }
            }
        }
        return content;
    }

    protected void getTitleAndPublished(String urlStr, Metadata metadata) throws DCTExtractorException, IOException{
        File wapitiBinaryFile = new File(wapitiBinaryPath);
        DCTExtractor extractor = new DCTExtractor(wapitiBinaryFile);
        Locale locale = Locale.ENGLISH;
        URL url = new URL(urlStr);
        HttpURLConnection connection = getConnection(url);
        long time = connection.getLastModified();
        try (InputStream is = connection.getInputStream()) {
            Calendar downloadDate;
            if (time > 0) {
                downloadDate = new GregorianCalendar();
                downloadDate.setTime(new Date(time));
            } else {
                downloadDate = new GregorianCalendar();
            }
            PageInfo pageInfo = extractor.getPageInfos(is, url, locale, downloadDate);

            // TODO Won't use title from DCTExtractor till the bug with codepage won't be fixed
            metadata.set(TikaCoreProperties.TITLE, "");
            // if we missed with locale, try to extract with russian locale because some sites calculate locale by IP
            if (pageInfo.getDCT() == null) {
                if (is != null) is.close();
                try (InputStream isNew = url.openStream()) {
                    locale = LanguageTools.getLocaleFromString("ru");
                    pageInfo = extractor.getPageInfos(isNew, url, locale, downloadDate);
                }
            }
            if (pageInfo.getDCT() != null) {
                metadata.set(TikaCoreProperties.CREATED, pageInfo.getDCT().getTime());
            }
            // if we have not found any date, set date from HTTP header
            if (pageInfo.getDCT() == null && time > 0) {
                metadata.set(TikaCoreProperties.CREATED, new Date(time));
            }
        }
    }

    private static class InterruptThread implements Runnable {
        Thread parent;
        HttpURLConnection con;
        int timeout;

        public InterruptThread(Thread parent, HttpURLConnection con, int timeout) {
            this.parent = parent;
            this.con = con;
            this.timeout = timeout;
        }

        public void run() {
            try {
                Thread.sleep(this.timeout);
            } catch (InterruptedException e) {
                // do nothing
            }

            // check if the connection had been closed properly by the main thread
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private static HttpURLConnection getConnection(URL url) throws IOException{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestProperty("Connection", "close");
        new Thread(new InterruptThread(Thread.currentThread(), connection, READ_TIMEOUT)).start();
        return connection;
    }

}
