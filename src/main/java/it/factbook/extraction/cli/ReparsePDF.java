package it.factbook.extraction.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.factbook.dictionary.Golem;
import it.factbook.extraction.Link;
import it.factbook.extraction.client.SearchEngine;
import it.factbook.extraction.config.AmqpConfig;
import it.factbook.extraction.message.SearchResultsMessage;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class ReparsePDF {
    static ObjectMapper jsonMapper = new ObjectMapper();

    public static void main(String[] args){
        PropertiesConfiguration buildConfig = null;
        try {
            buildConfig = new PropertiesConfiguration("build.properties");
            buildConfig.load();
            ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(Class.forName(buildConfig.getString("build.profile")));
            DataSource extractionDS = (DataSource)ctx.getBean("extractionDataSource");
            JdbcTemplate jdbcTemplate = new JdbcTemplate(extractionDS);
            AmqpTemplate amqpTemplate = ctx.getBean(AmqpTemplate.class);
            final String SELECT = "SELECT cl.url, MAX(cl.golemId) AS golemId, MAX(cl.profileId) AS profileId, MAX(cl.searchEngineId) AS searchEngineId, " +
                    "MAX(cl.requestLogId) AS requestLogId, d.title  " +
                    "FROM extraction.CrawlerLog cl " +
                    "LEFT JOIN doccache.Document d ON cl.urlHash = d.urlHash " +
                    "WHERE cl.requestLogId <> 0 AND cl.url like '%.pdf' AND cl.downloadSizeByte > 0 " +
                    "GROUP BY cl.url";
            List<SearchResultsMessage> crawlerMessages = jdbcTemplate.query(SELECT,
                    (row, nm) -> {
                        SearchResultsMessage msg = new SearchResultsMessage();
                        msg.setProfileId(row.getLong("profileId"));
                        msg.setRequestLogId(row.getLong("requestLogId"));
                        msg.setSearchEngine(SearchEngine.valueOf(row.getInt("searchEngineId")));
                        msg.setLinks(Arrays.asList(new Link(row.getString("url"), row.getString("title"), "", Golem.valueOf(row.getInt("golemId")))));
                        return msg;
                    });
            for (SearchResultsMessage msg: crawlerMessages){
                String json = jsonMapper.writeValueAsString(msg);
                amqpTemplate.convertAndSend(AmqpConfig.crawlerExchange().getName(), "#", json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
