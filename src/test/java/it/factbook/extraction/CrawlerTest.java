package it.factbook.extraction;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;


public class CrawlerTest {
    private static Crawler crawler = new Crawler();

    @Test
    public void testDownloadArticle() throws Exception {
        String text =  crawler.downloadArticle("http://www.huffingtonpost.com/2013/08/30/best-sleep-apps_n_3691693.html");
        //String text = crawler.downloadArticle("http://msk-tour.com/chto-interesnogo/item/улица-варварка-2");
        assertTrue(text.length() > 1000);
        assertTrue(text.indexOf("The pzizz app") > 0);
    }

    @Test
    public void testDownloadPdfArticle() throws Exception{
        String text = crawler.downloadArticle("http://www.cogsci.ucsd.edu/~pineda/COGS175/readings/Siegel.pdf");
        assertTrue(text.length() > 1000);
        assertTrue(text.indexOf("The stereotaxic instrument") > 0);
    }

    @Test(expected = IOException.class)
    public void testDownloadWithErrorResponseCode() throws Exception{
        crawler.downloadArticle("error.orange.es/ewe");
    }
}