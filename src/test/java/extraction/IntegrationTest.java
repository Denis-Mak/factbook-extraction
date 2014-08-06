package extraction;

import config.AmqpConfig;
import config.DataSourceConfigurationTest;
import it.factbook.search.repository.DocumentRepositoryConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.sql.DataSource;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=DataSourceConfigurationTest.class, loader=AnnotationConfigContextLoader.class)
public class IntegrationTest implements Runnable {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("extractionDataSource")
    private void setJdbcTemplate(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private JdbcTemplate sphinxTemplate;

    @Autowired
    @Qualifier("sphinxDataSource")
    private void setSphinxTemplate(DataSource dataSource){
        this.sphinxTemplate = new JdbcTemplate(dataSource);
    }

    @Autowired
    DocumentRepositoryConfig config;

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    AmqpAdmin amqpAdmin;

    private volatile boolean cancelled = false;

    @Before
    public void cleanTable(){
        jdbcTemplate.execute("TRUNCATE TABLE CrawlerLog");
        jdbcTemplate.execute("TRUNCATE TABLE DocumentContent");
        jdbcTemplate.execute("TRUNCATE TABLE Fact");
        jdbcTemplate.execute("TRUNCATE TABLE Document");
        sphinxTemplate.execute("TRUNCATE RTINDEX " + config.rtIndexes.get(1).get(0));
        amqpAdmin.purgeQueue(AmqpConfig.farooQueue().getName(), false);
        amqpAdmin.purgeQueue(AmqpConfig.crawlerQueue().getName(), false);
        amqpAdmin.purgeQueue(AmqpConfig.factSaverQueue().getName(), false);
        amqpAdmin.purgeQueue(AmqpConfig.indexUpdaterQueue().getName(), false);
    }

    @Test
    public void run() {
        amqpTemplate.convertAndSend("faroo-query", MessageFixtures.profileMessageJson);

        while (!cancelled) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
