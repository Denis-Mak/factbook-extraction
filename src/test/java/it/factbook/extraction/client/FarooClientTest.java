package it.factbook.extraction.client;

import it.factbook.extraction.MessageFixtures;
import it.factbook.extraction.config.ConfigPropertiesTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= ConfigPropertiesTest.class, loader=AnnotationConfigContextLoader.class)
public class FarooClientTest extends AbstractSearchEngineClientTest{
    @Autowired
    private FarooClient client;

    @Override
    protected AbstractSearchEngineClient getClient(){
        return client;
    }

    @Override
    @Test
    // Test is override because Faroo doesn't support russian language
    public void testGetQueries(){
        List<Request> queries = getClient().getQueries(MessageFixtures.profileMessage);
        assertEquals(2, queries.size());
        assertEquals("mobile ios iphone", queries.get(0).query);
        assertEquals("mobile power consumption", queries.get(1).query);
    }
}