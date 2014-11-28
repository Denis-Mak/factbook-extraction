package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.extraction.MessageFixtures;
import it.factbook.extraction.config.ConfigPropertiesTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= ConfigPropertiesTest.class, loader=AnnotationConfigContextLoader.class)
public class BingClientTest {
    @Autowired
    private BingClient bc;

    @Test
    public void testGetQueries() throws Exception {
        List<Query> queries = bc.getQueries(MessageFixtures.profileMessage);
        assertEquals(2, queries.size());
    }

    @Test
    @Ignore
    public void testGetLinks() throws Exception {
        List<Link> links = bc.getLinks(new Query(Golem.WIKI_EN, "iphone ios"));
        assertEquals(50, links.size());
    }
}
