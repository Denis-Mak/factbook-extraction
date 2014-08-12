package it.factbook.extraction.client;

import it.factbook.extraction.Link;
import it.factbook.extraction.MessageFixtures;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class BingClientTest {
    BingClient bc = new BingClient();

    @Test
    public void testGetQueries() throws Exception {
        List<Query> queries = bc.getQueries(MessageFixtures.profileMessage);
        assertEquals(2, queries.size());
    }

    @Test
    @Ignore
    public void testGetLinks() throws Exception {
        List<Link> links = bc.getLinks(new Query(2, "iphone ios"));
        assertEquals(50, links.size());
    }
}
