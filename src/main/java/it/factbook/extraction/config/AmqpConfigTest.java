package it.factbook.extraction.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class AmqpConfigTest extends AmqpConfig {
    @Override
    @Bean
    public Queue clusterProcessorQueue() {return new Queue("facts-to-cluster-query-test");}

    @Override
    @Bean
    public FanoutExchange clusterProcessorExchange(){
        return new FanoutExchange("facts-exchange-test");
    }
}
