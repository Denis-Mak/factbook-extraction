package it.factbook.extraction.config;

import it.factbook.extraction.Crawler;
import it.factbook.extraction.FactSaver;
import it.factbook.extraction.IndexUpdater;
import it.factbook.extraction.client.BingClient;
import it.factbook.extraction.client.FarooClient;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.amqp.core.BindingBuilder.bind;


/**
 *
 */
@Configuration
public class AmqpConfig {
    @Value("${amqp.url}")
    private String amqpUrl;

    @Value("${amqp.username}")
    private String amqpUsername;

    @Value("${amqp.password}")
    private String amqpPassword;

    //General MQ beans
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory(amqpUrl);
        cf.setUsername(amqpUsername);
        cf.setPassword(amqpPassword);
        return cf;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {return new RabbitAdmin(connectionFactory());}

    // //////////////////////
    // Search engine clients
    @Bean
    public static TopicExchange searchExtractionExchange() {
        return new TopicExchange("search-it.factbook.extraction-requests");
    }

    // Faroo
    @Bean
    public static Queue farooQueue() {
        return new Queue("faroo-query");
    }

    @Bean
    Binding bindingFaroo() {
        return bind(farooQueue()).to(searchExtractionExchange()).with("*");
    }

    @Bean
    SimpleMessageListenerContainer farooClientContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(farooQueue());
        container.setMessageListener(farooClient());
        return container;
    }

    @Bean
    MessageListener farooClient() {
        return new FarooClient();
    }

    //Bing
    @Bean
    public static Queue bingQueue() {
        return new Queue("bing-query");
    }

    @Bean
    Binding bindingBing() {
        return bind(bingQueue()).to(searchExtractionExchange()).with("*");
    }

    @Bean
    SimpleMessageListenerContainer bingClientContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(bingQueue());
        container.setMessageListener(bingClient());
        return container;
    }

    @Bean
    MessageListener bingClient() {
        return new BingClient();
    }

    // ////////////
    // Crawler
    @Bean
    public static Queue crawlerQueue() {
        return new Queue("links-for-crawler-query");
    }

    @Bean
    public static FanoutExchange crawlerExchange() {return new FanoutExchange("links-for-crawler-exchange");}

    @Bean
    Binding bindingCrawler() {
        return bind(crawlerQueue()).to(crawlerExchange());
    }

    @Bean
    SimpleMessageListenerContainer crawlerContainer(){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(crawlerQueue());
        container.setMessageListener(crawler());
        return container;
    }

    @Bean
    MessageListener crawler(){
        return new Crawler();
    }

    // ///////////
    // FactSaver
    @Bean
    public static Queue factSaverQueue() {
        return new Queue("documents-for-fact-saver-query");
    }

    @Bean
    public static FanoutExchange factSaverExchange() { return new FanoutExchange("documents-for-fact-saver-exchange");}

    @Bean
    Binding bindingFactSaver(){ return bind(factSaverQueue()).to(factSaverExchange());}

    @Bean
    SimpleMessageListenerContainer factSaverContainer(){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(factSaverQueue());
        container.setMessageListener(factSaver());
        container.setMaxConcurrentConsumers(10);
        return container;
    }

    @Bean
    MessageListener factSaver(){return new FactSaver();}

    // ////////////
    //IndexUpdater
    @Bean
    public static Queue indexUpdaterQueue() {return new Queue("facts-add-to-index-query");}

    @Bean
    public static FanoutExchange indexUpdaterExchange(){
        return new FanoutExchange("facts-add-to-index-exchange");
    }

    @Bean
    Binding bindingIndexUpdater(){return bind(indexUpdaterQueue()).to(indexUpdaterExchange());}

    @Bean
    SimpleMessageListenerContainer indexUpdaterContainer(){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(indexUpdaterQueue());
        container.setMessageListener(indexUpdater());
        return container;
    }

    @Bean
    MessageListener indexUpdater(){return new IndexUpdater();}
}
