package it.factbook.extraction.client;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.extraction.config.DataSourceConfigurationTest;
import org.junit.Ignore;
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
public class GoogleClientTest {
    @Autowired
    GoogleClient googleClient;

    @Test
    @Ignore
    public void testGetLinks() throws Exception {
        List<Link> links = googleClient.getLinks(new Query(Golem.WIKI_EN, "iphone ios"));
        assertEquals(10, links.size());
    }
}