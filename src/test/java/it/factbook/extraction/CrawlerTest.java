package it.factbook.extraction;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CrawlerTest {
    private static Crawler crawler = new Crawler();
    @Test
    public void testDownloadArticle() throws Exception {
        String text = crawler.downloadArticle("http://www.huffingtonpost.com/2013/08/30/best-sleep-apps_n_3691693.html");
        //String text = crawler.downloadArticle("http://lifehacker.com/deals-otterbox-iphone-cases-discounted-waterpiks-pre-1616845891");
        assertTrue(text.length() > 1000);
        assertTrue(text.indexOf("The pzizz app") > 0);
    }

    @Test
    public void testDownloadPdfArticle(){
        String text = crawler.downloadArticle("http://www.cogsci.ucsd.edu/~pineda/COGS175/readings/Siegel.pdf");
        assertTrue(text.length() > 1000);
        assertTrue(text.indexOf("The stereotaxic instrument") > 0);
    }
}