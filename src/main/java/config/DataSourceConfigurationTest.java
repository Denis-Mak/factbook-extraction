package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 */
@Configuration
@Import({AmqpConfig.class, DataSourceConfig.class})
public class DataSourceConfigurationTest extends AbstractBusinessConfig {
    private static final String DB_PROPERTY_FILE        = "jdbc.test.properties";
    private static final String SPHINX_PROPERTY_FILE    = "contextSearch.test.properties";
    private static final String AMQP_PROPERTY_FILE      = "amqp.test.properties";
}
