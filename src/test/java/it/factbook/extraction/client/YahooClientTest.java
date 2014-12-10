package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.extraction.MessageFixtures;
import it.factbook.extraction.config.ConfigPropertiesTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= ConfigPropertiesTest.class, loader=AnnotationConfigContextLoader.class)
public class YahooClientTest {
    @Autowired
    @Qualifier("yahooClient")
    private YahooClient yahooClient;

    @Test
    @Ignore
    public void testGetLinks() throws Exception {
        List<Link> links = yahooClient.getLinks(new Query(Golem.WIKI_RU, "+mobile ios iphone OR +mobile power consumption"));
        assertEquals(50, links.size());
    }

    @Test
    public void testGetQueries(){
        List<Query> queries = yahooClient.getQueries(MessageFixtures.profileMessage);
        assertEquals(2, queries.size());
        assertEquals("+mobile телефон эпл", queries.get(0).query);
        assertEquals("+mobile ios iphone OR +mobile power consumption", queries.get(1).query);

        ClientTestUtils.testGetQueryForProfileWithoutLines(yahooClient);
    }
}