package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.extraction.MessageFixtures;
import it.factbook.extraction.config.ConfigPropertiesTest;
import it.factbook.extraction.message.ProfileMessage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= ConfigPropertiesTest.class, loader=AnnotationConfigContextLoader.class)
public class FarooClientTest{
    @Autowired
    @Qualifier("farooClient")
    private FarooClient farooClient;

    @Test
    @Ignore
    public void testGetLinks() throws Exception {
       List<Link> links = farooClient.getLinks(new Query(Golem.WIKI_EN, "iphone ios"));
       assertEquals(10, links.size());
    }

    @Test
    public void testGetQueries() throws Exception {
        List<Query> queries = farooClient.getQueries(MessageFixtures.profileMessage);
        assertEquals(2, queries.size());
        assertEquals("mobile ios iphone", queries.get(0).query);
        assertEquals("mobile power consumption", queries.get(1).query);

        ClientTestUtils.testGetQueryForProfileWithoutLines(farooClient);

        ProfileMessage messageWithRuInitialQuery = new ProfileMessage();
        messageWithRuInitialQuery.setProfileId(2);
        messageWithRuInitialQuery.setInitialQuery("мама мыла раму");
        queries = farooClient.getQueries(messageWithRuInitialQuery);
        assertEquals(Collections.<Query>emptyList(), queries);
    }
}