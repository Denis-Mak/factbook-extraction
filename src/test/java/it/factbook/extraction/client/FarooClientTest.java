package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.extraction.MessageFixtures;
import it.factbook.extraction.config.DataSourceConfigurationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=DataSourceConfigurationTest.class, loader=AnnotationConfigContextLoader.class)
public class FarooClientTest {
    @Autowired
    private FarooClient fc;

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