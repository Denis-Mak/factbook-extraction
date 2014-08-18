package it.factbook.extraction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 */
@Configuration
@Import ({AmqpConfig.class, DataSourceConfig.class})
public class DataSourceConfigurationProduction extends AbstractBusinessConfig {
    @Override
    protected String dbPropertyFile() {return "jdbc.properties"; }

    @Override
    protected String sphinxPropertyFile() {return "contextSearch.properties"; }

    @Override
    protected String amqpPropertyFile() {return "amqp.properties";}
}
