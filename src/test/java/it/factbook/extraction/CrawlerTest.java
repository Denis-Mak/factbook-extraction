package it.factbook.extraction;

import it.factbook.extraction.config.ConfigPropertiesTest;
import it.factbook.search.repository.FactAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= ConfigPropertiesTest.class, loader=AnnotationConfigContextLoader.class)
public class CrawlerTest {
    private static Crawler crawler = new Crawler();

    @Autowired
    FactAdapter factAdapter;

    @Test
    public void testDownloadArticle() throws Exception {
        String text =  crawler.downloadArticle("http://www.factsearch.ru/");
        //String text = crawler.downloadArticle("http://www.ohsu.edu/nod/documents/2009/05-29/Kreitzer%202008.pdf");
        assertTrue(text.length() > 500);
        assertTrue(text.indexOf("Collect facts") > 0);
    }

    @Test
    public void testDownloadPdfArticle() throws Exception{
        String text = crawler.downloadArticle("http://www.cogsci.ucsd.edu/~pineda/COGS175/readings/Siegel.pdf");
        //String text = crawler.downloadArticle("http://www.ohsu.edu/nod/documents/2009/05-29/Kreitzer%202008.pdf");
        assertTrue(text.length() > 1000);
        assertTrue(text.indexOf("The stereotaxic instrument") > 0);
    }

    @Test(expected = IOException.class)
    public void testDownloadWithErrorResponseCode() throws Exception{
        crawler.downloadArticle("error.orange.es/ewe");
    }
}