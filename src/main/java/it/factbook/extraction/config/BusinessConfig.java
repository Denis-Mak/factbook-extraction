package it.factbook.extraction.config;

import com.jolbox.bonecp.BoneCPDataSource;
import it.factbook.dictionary.LangDetector;
import it.factbook.dictionary.LangDetectorCybozuImpl;
import it.factbook.dictionary.parse.TreeParser;
import it.factbook.dictionary.repository.StemAdapter;
import it.factbook.dictionary.repository.WordFormAdapter;
import it.factbook.dictionary.repository.jdbc.StemAdapterJdbcImpl;
import it.factbook.dictionary.repository.jdbc.WordFormAdapterJdbcImpl;
import it.factbook.extraction.ClusterProcessor;
import it.factbook.extraction.CrawlerLog;
import it.factbook.search.DocumentToFactSplitter;
import it.factbook.search.repository.ClusterAdapter;
import it.factbook.search.repository.DocumentRepositoryConfig;
import it.factbook.search.repository.FactAdapter;
import it.factbook.search.repository.jdbc.ClusterAdapterJdbcImpl;
import it.factbook.search.repository.jdbc.FactAdapterJdbcImpl;
import it.factbook.sphinx.SphinxIndexUpdater;
import it.factbook.util.TextSplitter;
import it.factbook.util.TextSplitterOpenNlpRuImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 *
 */
@Configuration
public class BusinessConfig {
    @Value("${jdbc.doccache.url}")
    private String jdbcDoccacheUrl;

    @Value("${jdbc.doccache.username}")
    private String jdbcDoccacheUsername;

    @Value("${jdbc.doccache.password}")
    private String jdbcDoccachePassword;

    @Value("${jdbc.extraction.url}")
    private String jdbcExtractionUrl;

    @Value("${jdbc.extraction.username}")
    private String jdbcExtractionUsername;

    @Value("${jdbc.extraction.password}")
    private String jdbcExtractionPassword;

    @Value("${jdbc.dictionary.url}")
    private String jdbcDictionaryUrl;

    @Value("${jdbc.dictionary.username}")
    private String jdbcDictionaryUsername;

    @Value("${jdbc.dictionary.password}")
    private String jdbcDictionaryPassword;

    @Value("${csHost}")
    private String csHost;

    @Value("${csPort}")
    private String csPort;

    @Value("${csSqlPort}")
    private String csSqlPort;

    //Database datasources
    @Bean (name = "doccacheDataSource")
    public DataSource doccacheDataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(jdbcDoccacheUrl);
        dataSource.setUsername(jdbcDoccacheUsername);
        dataSource.setPassword(jdbcDoccachePassword);

        return dataSource;
    }

    @Bean (name = "extractionDataSource")
    public DataSource extractionDataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(jdbcExtractionUrl);
        dataSource.setUsername(jdbcExtractionUsername);
        dataSource.setPassword(jdbcExtractionPassword);

        return dataSource;
    }

    @Bean (name = "dictionaryDataSource")
    public DataSource dictionaryDataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(jdbcDictionaryUrl);
        dataSource.setUsername(jdbcDictionaryUsername);
        dataSource.setPassword(jdbcDictionaryPassword);

        return dataSource;
    }

    //Sphinx datasource
    @Bean (name = {"sphinxDataSource", "sphinxQlConnection"})
    public DataSource sphinxDataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://" + csHost + ":" + csSqlPort + "/");
        dataSource.setUsername("");
        dataSource.setPassword("");

        return dataSource;
    }

    @Bean(initMethod = "init")
    public static DocumentRepositoryConfig documentRepositoryConfig(){
        return new DocumentRepositoryConfig();
    }

    //Helper beans
    @Bean
    public DocumentToFactSplitter documentToFactSplitter(){return new DocumentToFactSplitter();}

    @Bean
    public FactAdapter factAdapter() {return new FactAdapterJdbcImpl();}

    @Bean
    public CrawlerLog crawlerLog(){
        return new CrawlerLog();
    }

    @Bean
    public SphinxIndexUpdater sphinxIndexUpdater() {return new SphinxIndexUpdater();}

    @Bean
    public TextSplitter textSplitter() {return new TextSplitterOpenNlpRuImpl();}

    @Bean
    public LangDetector langDetector() {return new LangDetectorCybozuImpl();}

    @Bean
    public WordFormAdapter wordFormAdapter() {return new WordFormAdapterJdbcImpl();}

    @Bean
    public StemAdapter stemAdapter() {return new StemAdapterJdbcImpl();}

    @Bean
    public ClusterAdapter clusterAdapter() {return new ClusterAdapterJdbcImpl();}

    @Bean
    public TreeParser treeParser() {return new TreeParser();}

    @Bean
    public ClusterProcessor clusterProcessor() {return new ClusterProcessor();}
}
