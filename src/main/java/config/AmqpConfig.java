package config;

import extraction.Crawler;
import extraction.FactSaver;
import extraction.IndexUpdater;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.client.FarooClient;

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
    public static Queue farooQueue() {
        return new Queue("faroo-query");
    }

    @Bean
    public static TopicExchange searchExtractionExchange() {
        return new TopicExchange("search-extraction-requests");
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
