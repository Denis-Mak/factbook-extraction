package it.factbook.extraction.config;

import it.factbook.extraction.FactSaver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class AmqpConfigTest extends AmqpConfig {
    @Bean
    FactSaver factSaver(){return new FactSaver();}
}
