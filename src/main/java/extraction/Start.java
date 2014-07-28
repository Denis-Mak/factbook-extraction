package extraction;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 */
public class Start {
    public static void main (String[] ars) throws Exception{
        ApplicationContext ctx = new AnnotationConfigApplicationContext(MessageQueueConfiguration.class);
        System.out.println("Waiting five seconds...");
        Thread.sleep(5000);
        System.out.println("Sending message...");
        AmqpTemplate rabbitTemplate = ctx.getBean(AmqpTemplate.class);
        rabbitTemplate.convertAndSend("search-extraction-requests", "faroo-query", "New Message!");
        System.out.println("Message sent.");
    }
}
