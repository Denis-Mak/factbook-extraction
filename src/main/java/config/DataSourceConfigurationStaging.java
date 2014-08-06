package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 */
@Configuration
@Import ({AmqpConfig.class, DataSourceConfig.class})
public class DataSourceConfigurationStaging extends AbstractBusinessConfig {
    private static final String DB_PROPERTY_FILE        = "jdbc.staging.properties";
    private static final String SPHINX_PROPERTY_FILE    = "contextSearch.staging.properties";
    private static final String AMQP_PROPERTY_FILE      = "amqp.staging.properties";
}
