package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.extraction.MessageFixtures;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FarooClientTest {
    FarooClient fc = new FarooClient();

    @Test
    public void testGetQueries() throws Exception {
        List<Query> queries = fc.getQueries(MessageFixtures.profileMessage);
        assertEquals(2, queries.size());
    }

    @Test
    public void testGetLinks() throws Exception {
       List<Link> links = fc.getLinks(new Query(Golem.WIKI_EN, "iphone ios"));
       assertEquals(10, links.size());
    }
}