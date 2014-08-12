package it.factbook.extraction.config;

import it.factbook.extraction.CrawlerLog;
import it.factbook.dictionary.repository.WordFormAdapter;
import it.factbook.dictionary.repository.jdbc.WordFormAdapterJdbcImpl;
import it.factbook.search.DocumentToFactSplitter;
import it.factbook.search.repository.DocumentRepositoryConfig;
import it.factbook.search.repository.FactAdapter;
import it.factbook.search.repository.jdbc.FactAdapterJdbcImpl;
import it.factbook.sphinx.SphinxIndexUpdater;
import it.factbook.util.LangDetector;
import it.factbook.util.LangDetectorCybozuImpl;
import it.factbook.util.TextSplitter;
import it.factbook.util.TextSplitterOpenNlpRuImpl;
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
public abstract class AbstractBusinessConfig {
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

    @Bean(initMethod = "init")
    public static DocumentRepositoryConfig documentRepositoryConfig(){
        return new DocumentRepositoryConfig();
    }

    //Helper beans
    @Bean
    public DocumentToFactSplitter documentToFactSplitter(){return new DocumentToFactSplitter();}

    @Bean
    public FactAdapter factAdapter() {return new FactAdapterJdbcImpl();}

    @Bean
    public CrawlerLog crawlerLog(){
        return new CrawlerLog();
    }

    @Bean
    public SphinxIndexUpdater sphinxIndexUpdater() {return new SphinxIndexUpdater();}

    @Bean
    public TextSplitter textSplitter() {return new TextSplitterOpenNlpRuImpl();}

    @Bean
    public LangDetector langDetector() {return new LangDetectorCybozuImpl();}

    @Bean
    public WordFormAdapter wordFormAdapter() {return new WordFormAdapterJdbcImpl();}
}
