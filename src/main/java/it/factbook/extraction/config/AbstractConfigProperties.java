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
    protected abstract String sphinxPropertyFile();
    protected abstract String amqpPropertyFile();

    @Bean
    public PropertySourcesPlaceholderConfigurer propertiesPlaceholderConfigurer(){
        PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
        Resource[] resourceLocations = new Resource[] {
                new ClassPathResource(dbPropertyFile()),
                new ClassPathResource(sphinxPropertyFile()),
                new ClassPathResource(amqpPropertyFile()),
        };
        bean.setLocations(resourceLocations);
        return bean;
    }

    @Bean
    public PropertiesFactoryBean propertiesHolder(){
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource(sphinxPropertyFile()));
        return bean;
    }


}
