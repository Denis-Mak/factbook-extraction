package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
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
public class GoogleClientTest{
    @Autowired
    @Qualifier("googleClient")
    GoogleClient googleClient;

    @Test
    @Ignore
    public void testGetLinks() throws Exception {
        List<Link> links = googleClient.getLinks(new Query(Golem.WIKI_RU, "прогноз развития фондового рынка ((российскими | акциями) | (2008 | г.))"));
        assertEquals(10, links.size());
    }

    @Test
    public void testGetQueries(){
        ClientTestUtils.testGetQueries(googleClient);
        ClientTestUtils.testGetQueryForProfileWithoutLines(googleClient);
    }
}