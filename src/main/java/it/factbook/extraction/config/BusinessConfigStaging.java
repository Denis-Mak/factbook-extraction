package it.factbook.extraction.config;

import it.factbook.dictionary.repository.StemAdapter;
import it.factbook.dictionary.repository.WordFormAdapter;
import it.factbook.dictionary.repository.inmemory.StemAdapterInMemoryImpl;
import it.factbook.dictionary.repository.inmemory.WordFormAdapterInMemoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class BusinessConfigStaging extends BusinessConfig {
    @Override
    @Bean
    public StemAdapter stemAdapter(){
        return new StemAdapterInMemoryImpl(this.dictionaryDataSource());
    }

    @Override
    @Bean
    public WordFormAdapter wordFormAdapter(){
        return new WordFormAdapterInMemoryImpl(this.dictionaryDataSource());
    }
}
