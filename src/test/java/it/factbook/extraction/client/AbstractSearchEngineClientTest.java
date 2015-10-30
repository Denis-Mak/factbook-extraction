package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.extraction.MessageFixtures;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public abstract class AbstractSearchEngineClientTest {

    protected abstract AbstractSearchEngineClient getClient();

    @Test
    public void checkPreviousResults() {
        assertEquals(new Integer(0), getClient().getStartRecordNm(Collections.<Request>emptyList()));

        List<Request> requestList = new ArrayList<>(4);
        requestList.add(new Request(Golem.WIKI_EN, "query", 0, new DateTime()));
        requestList.add(new Request(Golem.WIKI_EN, "query", getClient().getMaxResultsPerPage(), new DateTime()));
        assertEquals(new Integer(2 * getClient().getMaxResultsPerPage()), getClient().getStartRecordNm(requestList));

        requestList.add(new Request(Golem.WIKI_EN, "query", 3 * getClient().getMaxResultsPerPage(), new DateTime()));
        assertEquals(null, getClient().getStartRecordNm(requestList));

        requestList.add(new Request(Golem.WIKI_EN, "query", 4 * getClient().getMaxResultsPerPage(), new DateTime()));
        assertEquals(null, getClient().getStartRecordNm(requestList));

        requestList = new ArrayList<>(2);
        requestList.add(new Request(Golem.WIKI_EN, "query", 0, new DateTime().minusMonths(2)));
        requestList.add(new Request(Golem.WIKI_EN, "query", getClient().getMaxResultsPerPage(), new DateTime()));
        assertEquals(new Integer(0), getClient().getStartRecordNm(requestList));
    }

    @Test
    public void testGetQueries(){
        List<Request> queries = getClient().getQueries(MessageFixtures.profileMessage);
        assertEquals(3, queries.size());
        assertEquals("mobile ios iphone", queries.get(0).query);
        assertEquals("mobile power consumption", queries.get(1).query);
        assertEquals("mobile телефон эпл", queries.get(2).query);
    }

    @Test
    public void testGetQueryForProfileWithoutLines(){
        List<Request> queries = getClient().getQueries(MessageFixtures.profileMessageWithoutLines);
        assertEquals(1, queries.size());
        assertEquals("maduro maio", queries.get(0).query);
    }

    @Test
    @Ignore
    public void testGetLinks() throws Exception {
        List<Link> links = getClient().getLinks(new Request(Golem.WIKI_EN, "iphone ios", 0, new DateTime()));
        assertEquals(getClient().getMaxResultsPerPage(), links.size());

        //get next page
        List<Link> nextPageLinks = getClient().getLinks(new Request(Golem.WIKI_EN, "iphone ios", getClient().getMaxResultsPerPage(), new DateTime()));
        assertEquals(getClient().getMaxResultsPerPage(), nextPageLinks.size());
        // check that it is really new page no links must be in nextPageResults
        // this code commented because it doesn't work for Yahoo and Bing
        // there are one or two URL that exist in both pages
        /*List<String> firstPageUrls = links.stream().map(Link::getUrl).collect(Collectors.toList());
        List<String> secondPageUrls = nextPageLinks.stream().map(Link::getUrl).collect(Collectors.toList());
        for(String url: firstPageUrls){
            boolean duplicate = secondPageUrls.contains(url);
            assertFalse(duplicate);
        }*/
    }
}
