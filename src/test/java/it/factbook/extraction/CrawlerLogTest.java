package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.client.Query;
import it.factbook.extraction.client.SearchEngine;
import it.factbook.extraction.config.DataSourceConfigurationTest;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=DataSourceConfigurationTest.class, loader=AnnotationConfigContextLoader.class)
public class CrawlerLogTest {
    private static final long PROFILE_ID = 1;
    private static final SearchEngine SE = SearchEngine.FAROO;
    private static final long PROFILE_VERSION = 111;
    private static List<Link> linksToLog = new ArrayList<>(3);
    static {
        linksToLog.add(new Link("http://www.era.com/articles/1", "Just a few words", " We are talking about iOS", Golem.WIKI_EN));
        linksToLog.add(new Link("http://www.xixi.com/new/vertu-on-sale", "Vertu won't be sold", " That's the news", Golem.WIKI_EN));
        linksToLog.add(new Link("http://www.hashara.com/about/", "Keep track our twitter", " Android and iOS news", Golem.WIKI_EN));
    }

    @Autowired
    private CrawlerLog crawlerLog;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("extractionDataSource")
    private void setJdbcTemplate(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @After
    public void cleanTable(){
        jdbcTemplate.execute("TRUNCATE TABLE CrawlerLog");
    }

    @Test
    public void testGetLinksToCrawl() throws Exception {
        crawlerLog.logFoundLinks(PROFILE_ID, SE, PROFILE_VERSION, linksToLog.subList(0,2));
        List<Link> linksToDownload = crawlerLog.getLinksToCrawl(linksToLog);
        assertEquals(1, linksToDownload.size());
        assertEquals("http://www.hashara.com/about/", linksToDownload.get(0).getUrl());
    }

    @Test
    public void testLogFoundLinks() throws Exception {
        crawlerLog.logFoundLinks(PROFILE_ID, SE, PROFILE_VERSION, linksToLog);
        List<Link> linksWritten = jdbcTemplate.query("SELECT profileId, searchEngineId, requestLogId, golemId, url " +
                "FROM CrawlerLog", (row, rowNm) -> new Link(row.getString("url"), "", "", Golem.valueOf(row.getInt("golemId"))));
        assertEquals(3, linksWritten.size());
    }

    @Test
    public void testLogDownloadedArticles() throws Exception {
        class Row {
            int downloadSizeByte;
            int downloadTimeMsec;
        }
        crawlerLog.logFoundLinks(PROFILE_ID, SE, PROFILE_VERSION, linksToLog);
        crawlerLog.logDownloadedArticles(PROFILE_ID, SE, PROFILE_VERSION, linksToLog.get(0).getUrl(),
                "Apple spent a significant amount of its WWDC 2014 keynote focusing on iOS 8, which takes the flat iOS 7 design and only rounds it out with new features.\n" +
                        "That means instead of a dramatic redesign, you can expect this year's mobile operating system update to tie everything together with the overarching theme of \"convergence.\"",
                new DateTime().minusMillis(2000).getMillis(), ""); // imitate download started 2 sec ago
        List<Row> linksWritten = jdbcTemplate.query("SELECT downloadSizeByte, downloadTimeMsec " +
                "FROM CrawlerLog WHERE urlHash = ?", new Object[]{linksToLog.get(0).getUrlHash()}, new int[]{Types.CHAR},
                (row, rowNm) -> {
                    Row r = new Row();
                    r.downloadSizeByte = row.getInt("downloadSizeByte");
                    r.downloadTimeMsec = row.getInt("downloadTimeMsec");
                    return r;
                });
        assertEquals(1, linksWritten.size());
        assertTrue(linksWritten.get(0).downloadTimeMsec > 2000 && linksWritten.get(0).downloadTimeMsec < 2100);
        assertEquals(325, linksWritten.get(0).downloadSizeByte);

        List<Row> linksUntouched = jdbcTemplate.query("SELECT downloadSizeByte, downloadTimeMsec " +
                        "FROM CrawlerLog WHERE urlHash <> ?", new Object[]{linksToLog.get(0).getUrlHash()}, new int[]{Types.CHAR},
                (row, rowNm) -> {
                    Row r = new Row();
                    r.downloadSizeByte = row.getInt("downloadSizeByte");
                    r.downloadTimeMsec = row.getInt("downloadTimeMsec");
                    return r;
                });
        assertEquals(2, linksUntouched.size());
        linksUntouched.stream().forEach(link -> {
                    assertEquals(0, link.downloadSizeByte);
                    assertEquals(0, link.downloadTimeMsec);
                });

    }

    @Test
    public void testLogRequest(){
        jdbcTemplate.execute("TRUNCATE TABLE RequestLog");
        long requestId = crawlerLog.logSearchRequest(PROFILE_ID, SE, PROFILE_VERSION, new Query(Golem.WIKI_EN,"query"));
        long rowCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM RequestLog", (row, rowNm) -> row.getLong(1));
        assertEquals(1, rowCount);
        assertTrue(requestId > 0);
    }
}