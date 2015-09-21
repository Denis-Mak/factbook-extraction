package it.factbook.extraction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 */
@Configuration
@Import ({AmqpConfig.class, BusinessConfigInMemory.class})
public class ConfigPropertiesProduction extends AbstractConfigProperties {
    @Override
    protected String dbPropertyFile() {return "jdbc.properties"; }

    @Override
    protected String searchEnginePropertyFile() {return "searchEngine.properties"; }

    @Override
    protected String amqpPropertyFile() {return "amqp.properties";}

    @Override
    protected String cassandraPropertyFile() {return "cql.properties";}
}
