package it.factbook.extraction.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import it.factbook.extraction.FactsMessage;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.config.ConfigPropertiesStaging;
import it.factbook.search.Fact;
import it.factbook.search.repository.FactAdapter;
import it.factbook.util.DbUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public class ClusterFacts {
    private static RabbitAdmin admin;
    private static ObjectMapper jsonMapper = new ObjectMapper();


    public static void main(String[] ars){
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(ConfigPropertiesStaging.class);
        admin = context.getBean(RabbitAdmin.class);
        FactAdapter factAdapter = context.getBean(FactAdapter.class);
        AmqpTemplate amqpTemplate = context.getBean(AmqpTemplate.class);
        AmqpConfig amqpConfig = context.getBean(AmqpConfig.class);
        DataSource doccacheDataSource = (DataSource)context.getBean("doccacheDataSource");
        int batchSize;
        int i=0;
        long lastFactId = 0;
        try {
            DbUtils.disableKeys(doccacheDataSource, "Fact");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        do{
            FactsMessage factsMessage = new FactsMessage();
            List<Fact> factBatch = factAdapter.getFactBatch(i*100,100);
            factsMessage.setFacts(factBatch);
            batchSize = factBatch.size();
            lastFactId = factBatch.get(batchSize - 1).getId();
            if (batchSize > 0) {
                jsonMapper.registerModule(new JodaModule());
                try {
                    String json = jsonMapper.writeValueAsString(factsMessage);
                    amqpTemplate.convertAndSend(amqpConfig.clusterProcessorExchange().getName(), "#", json);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                i++;
            }
        } while (batchSize > 0);
        System.out.println("Last fact ID is " + lastFactId);
        while (getQueueCount(amqpConfig.clusterProcessorQueue().getName()) > 0){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            DbUtils.enableKeys(doccacheDataSource, "Fact");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
    }

    protected static int getQueueCount(final String name) {
        DeclareOk declareOk = admin.getRabbitTemplate().execute(channel -> channel.queueDeclarePassive(name));
        return declareOk.getMessageCount();
    }
}
