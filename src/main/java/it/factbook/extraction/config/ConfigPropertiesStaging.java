package it.factbook.extraction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 */
@Configuration
@Import({AmqpConfig.class, BusinessConfig.class})
public class ConfigPropertiesStaging extends AbstractConfigProperties {

    @Override
    protected String dbPropertyFile() {return "jdbc.staging.properties"; }

    @Override
    protected String sphinxPropertyFile() {return "contextSearch.staging.properties"; }

    @Override
    protected String amqpPropertyFile() {return "amqp.staging.properties";}

    @Override
    protected String cassandraPropertyFile() {return "cql.staging.properties";}
}
