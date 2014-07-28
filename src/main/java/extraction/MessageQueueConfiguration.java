package extraction;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.client.FarooClient;

import static org.springframework.amqp.core.BindingBuilder.bind;


/**
 *
 */
@Configuration
public class MessageQueueConfiguration {
    //General MQ beans
    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory("localhost");
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }


    // Search engine clients
    @Bean
    Queue farooQueue() {
        return new Queue("faroo-query");
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("search-extraction-requests");
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return bind(queue).to(exchange).with("*");
    }

    @Bean
    SimpleMessageListenerContainer farooClientContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueueNames("faroo-query");
        container.setMessageListener(farooClient());
        return container;
    }

    @Bean
    MessageListener farooClient() {
        return new FarooClient();
    }

    // Internal document processing
    @Bean
    Queue crawlerQueue() {
        return new Queue("links-for-crawler");
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
}
