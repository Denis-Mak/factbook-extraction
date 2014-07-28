package extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

/**
 *
 */
@Component
public class Crawler implements MessageListener{
    private static final Logger log = LoggerFactory.getLogger(Crawler.class);
    @Autowired
    AnnotationConfigApplicationContext context;

    private static ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message) {
        System.out.println("Crawler recieved: " + Arrays.toString(message.getBody()));
        SearchResultsMessage searchResultsMessage = new SearchResultsMessage();
        try {
            searchResultsMessage = jsonMapper.readValue(message.getBody(), SearchResultsMessage.class);
        } catch (IOException e) {
            log.error("Error during unpack SearchResultsMessage: {}", e);
        }
        for (Link link: searchResultsMessage.links){
            downloadArticle(link.getUrl());
        }

        this.context.close();
    }

    public String downloadArticle(String url){
        String content = "";
        try {
            URL myUrl = new URL(url);
            Metadata metadata = new Metadata();
            InputStream is = TikaInputStream.get(myUrl, metadata);
            String mimeType = new Tika().detect(is);
            switch (mimeType) {
                case "application/xhtml+xml": {
                    BodyContentHandler ch = new BodyContentHandler();
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
            is.close();
        } catch (Exception e){
            log.error("Error get results: {}", e);
        }

        return content;
    }
}
