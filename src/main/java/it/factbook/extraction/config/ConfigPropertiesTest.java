package it.factbook.extraction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 */
@Configuration
@Import({AmqpConfigTest.class, BusinessConfig.class})
public class ConfigPropertiesTest extends AbstractConfigProperties {
    @Override
    protected String dbPropertyFile() {return "jdbc.test.properties"; }

    @Override
    protected String sphinxPropertyFile() {return "searchEngine.test.properties"; }

    @Override
    protected String amqpPropertyFile() {return "amqp.test.properties";}

    @Override
    protected String cassandraPropertyFile() {return "cql.test.properties";}
}
