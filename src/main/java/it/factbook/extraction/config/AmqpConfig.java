package it.factbook.extraction.config;

import it.factbook.extraction.*;
import it.factbook.extraction.client.BingClient;
import it.factbook.extraction.client.FarooClient;
import it.factbook.extraction.client.GoogleClient;
import it.factbook.extraction.client.YahooClient;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
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
    FarooClient farooClient() {
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
    BingClient bingClient() {
        return new BingClient();
    }

    //Google
    @Bean
    public static Queue googleQueue() {
        return new Queue("google-query");
    }

    @Bean
    Binding bindingGoogle() {
        return bind(googleQueue()).to(searchExtractionExchange()).with("*");
    }

    @Bean
    SimpleMessageListenerContainer googleClientContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(googleQueue());
        container.setMessageListener(googleClient());
        return container;
    }

    @Bean
    GoogleClient googleClient() {
        return new GoogleClient();
    }

    //YAHOO
    @Bean
    public static Queue yahooQueue() {
        return new Queue("yahoo-query");
    }

    @Bean
    Binding bindingYahoo() {
        return bind(yahooQueue()).to(searchExtractionExchange()).with("*");
    }

    @Bean
    SimpleMessageListenerContainer yahooClientContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(yahooQueue());
        container.setMessageListener(yahooClient());
        return container;
    }

    @Bean
    YahooClient yahooClient() {
        return new YahooClient();
    }

    // ////////////
    // Crawler
    @Bean
    public static Queue crawlerQueue() {
        return new Queue("links-for-crawler-query");
    }

    @Bean
    public static FanoutExchange crawlerExchange() {return new FanoutExchange("links-exchange");}

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
    public static FanoutExchange factSaverExchange() { return new FanoutExchange("documents-exchange");}

    @Bean
    Binding bindingFactSaver(){ return bind(factSaverQueue()).to(factSaverExchange());}

    @Bean
    SimpleMessageListenerContainer factSaverContainer(){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(factSaverQueue());
        container.setMessageListener(factSaver());
    //    container.setMaxConcurrentConsumers(10);
        return container;
    }

    @Bean (name = "factSaver")
    MessageListener factSaver(){return new FactSaver();}

    // ////////////
    //IndexUpdater
    @Bean
    public static Queue indexUpdaterQueue() {return new Queue("facts-add-to-index-query");}

    @Bean
    public static FanoutExchange indexUpdaterExchange(){
        return new FanoutExchange("facts-clustered-exchange");
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

    //////////////////////
    // RPC Update profile
    @Bean
    SimpleMessageListenerContainer profileUpdaterRpcContainer(){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(rpcQueue());
        container.setMessageListener(new MessageListenerAdapter(profileUpdaterMsgHandler(), "handleMessage"));
        return container;
    }

    @Bean
    public Queue rpcQueue() {
        return new Queue("rpc-queue");
    }

    @Bean
    public ProfileUpdaterMsgHandler profileUpdaterMsgHandler() {return new ProfileUpdaterMsgHandler();}
}
