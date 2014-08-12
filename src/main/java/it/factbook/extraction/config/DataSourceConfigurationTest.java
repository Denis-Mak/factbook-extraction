package it.factbook.extraction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 */
@Configuration
@Import({AmqpConfig.class, DataSourceConfig.class})
public class DataSourceConfigurationTest extends AbstractBusinessConfig {
    @Override
    protected String dbPropertyFile() {return "jdbc.test.properties"; }

    @Override
    protected String sphinxPropertyFile() {return "contextSearch.test.properties"; }

    @Override
    protected String amqpPropertyFile() {return "amqp.test.properties";}
}
