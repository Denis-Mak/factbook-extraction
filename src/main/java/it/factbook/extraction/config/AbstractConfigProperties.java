package it.factbook.extraction.config;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 */
@Configuration
public abstract class AbstractConfigProperties {
    protected abstract String dbPropertyFile();
    protected abstract String searchEnginePropertyFile();
    protected abstract String amqpPropertyFile();
    protected abstract String cassandraPropertyFile();

    @Bean
    public PropertySourcesPlaceholderConfigurer propertiesPlaceholderConfigurer(){
        PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
        Resource[] resourceLocations = new Resource[] {
                new ClassPathResource(dbPropertyFile()),
                new ClassPathResource(searchEnginePropertyFile()),
                new ClassPathResource(amqpPropertyFile()),
                new ClassPathResource(cassandraPropertyFile())
        };
        bean.setLocations(resourceLocations);
        return bean;
    }

    @Bean
    public PropertiesFactoryBean propertiesHolder(){
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource(searchEnginePropertyFile()));
        return bean;
    }


}
