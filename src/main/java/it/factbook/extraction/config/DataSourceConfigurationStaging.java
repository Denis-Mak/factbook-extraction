package it.factbook.extraction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 */
@Configuration
@Import ({AmqpConfig.class, DataSourceConfig.class})
public class DataSourceConfigurationStaging extends AbstractBusinessConfig {
    @Override
    protected String dbPropertyFile() {return "jdbc.staging.properties"; }

    @Override
    protected String sphinxPropertyFile() {return "contextSearch.staging.properties"; }

    @Override
    protected String amqpPropertyFile() {return "amqp.staging.properties";}
}
