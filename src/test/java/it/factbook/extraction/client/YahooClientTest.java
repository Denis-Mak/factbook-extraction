package it.factbook.extraction.client;

import it.factbook.extraction.config.ConfigPropertiesTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= ConfigPropertiesTest.class, loader=AnnotationConfigContextLoader.class)
public class YahooClientTest extends AbstractSearchEngineClientTest{
    @Autowired
    private YahooClient client;

    @Override
    protected AbstractSearchEngineClient getClient(){
        return client;
    }
}