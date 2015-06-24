package it.factbook.extraction.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;
import com.jolbox.bonecp.BoneCPDataSource;
import it.factbook.dictionary.LangDetector;
import it.factbook.dictionary.LangDetectorCybozuImpl;
import it.factbook.dictionary.parse.TreeParser;
import it.factbook.dictionary.parse.TreeParserImpl;
import it.factbook.dictionary.repository.StemAdapter;
import it.factbook.dictionary.repository.WordFormAdapter;
import it.factbook.dictionary.repository.jdbc.StemAdapterJdbcImpl;
import it.factbook.dictionary.repository.jdbc.WordFormAdapterJdbcImpl;
import it.factbook.extraction.CrawlerLog;
import it.factbook.search.FactProcessor;
import it.factbook.search.SearchProfileUpdater;
import it.factbook.search.classifier.Classifier;
import it.factbook.search.repository.ClassifierAdapter;
import it.factbook.search.repository.DocumentRepositoryConfig;
import it.factbook.search.repository.FactAdapter;
import it.factbook.search.repository.SemanticSearch;
import it.factbook.search.repository.cassandra.FactAdapterCassandraImpl;
import it.factbook.search.repository.cassandra.SemanticSearchCassandraImpl;
import it.factbook.search.repository.jdbc.ClassifierAdapterImpl;
import it.factbook.search.repository.jdbc.SphinxSliceIndexAdapter;
import it.factbook.sphinx.SphinxIndexUpdater;
import it.factbook.util.TextSplitter;
import it.factbook.util.TextSplitterOpenNlpRuImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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

    @Value("${cassandra.contactpoints}")
    private String cassandraPoints;

    @Value("${cassandra.port}")
    private String cassandraPort;

    @Value("${cassandra.keyspace}")
    private String cassandraKeyspace;

    @Value("${cassandra.user}")
    private String cassandraUser;

    @Value("${cassandra.password}")
    private String cassandraPass;


    //Database datasources
    private void setCommonDataSourceParams(BoneCPDataSource dataSource){
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setIdleConnectionTestPeriodInMinutes(10);
        dataSource.setConnectionTestStatement("SELECT 1");
    }

    @Bean (name = "doccacheDataSource")
    public DataSource doccacheDataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        setCommonDataSourceParams(dataSource);
        dataSource.setJdbcUrl(jdbcDoccacheUrl);
        dataSource.setUsername(jdbcDoccacheUsername);
        dataSource.setPassword(jdbcDoccachePassword);

        return dataSource;
    }

    @Bean (name = "extractionDataSource")
    public DataSource extractionDataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        setCommonDataSourceParams(dataSource);
        dataSource.setJdbcUrl(jdbcExtractionUrl);
        dataSource.setUsername(jdbcExtractionUsername);
        dataSource.setPassword(jdbcExtractionPassword);


        return dataSource;
    }

    @Bean (name = "dictionaryDataSource")
    public DataSource dictionaryDataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        setCommonDataSourceParams(dataSource);
        dataSource.setJdbcUrl(jdbcDictionaryUrl);
        dataSource.setUsername(jdbcDictionaryUsername);
        dataSource.setPassword(jdbcDictionaryPassword);

        return dataSource;
    }

    //Sphinx datasource
    @Bean (name = {"sphinxDataSource", "sphinxQlConnection"})
    public DataSource sphinxDataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        setCommonDataSourceParams(dataSource);
        dataSource.setJdbcUrl("jdbc:mysql://" + csHost + ":" + csSqlPort + "/");
        dataSource.setUsername("");
        dataSource.setPassword("");

        return dataSource;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Session session(){
        Cluster cluster = Cluster.builder()
                        .addContactPoints(cassandraPoints.split(","))
                        .withPort(Integer.parseInt(cassandraPort))
                        .withAuthProvider(new PlainTextAuthProvider(cassandraUser, cassandraPass))
                        .build();
        return cluster.connect(cassandraKeyspace);
    }

    @Bean(initMethod = "init")
    public static DocumentRepositoryConfig documentRepositoryConfig(){
        return new DocumentRepositoryConfig();
    }

    //Helper beans
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FactProcessor factProcessor(){return new FactProcessor();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FactAdapter factAdapter() {return new FactAdapterCassandraImpl();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public CrawlerLog crawlerLog(){
        return new CrawlerLog();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SphinxIndexUpdater sphinxIndexUpdater() {return new SphinxIndexUpdater();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TextSplitter textSplitter() {return new TextSplitterOpenNlpRuImpl();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public LangDetector langDetector() {return new LangDetectorCybozuImpl();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public WordFormAdapter wordFormAdapter() {return new WordFormAdapterJdbcImpl();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public StemAdapter stemAdapter() {return new StemAdapterJdbcImpl();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TreeParser treeParser() {return new TreeParserImpl();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SearchProfileUpdater profileUpdater() {return new SearchProfileUpdater();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Classifier classifier() {return new Classifier();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ClassifierAdapter classifierAdapter() {return new ClassifierAdapterImpl(doccacheDataSource());}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SphinxSliceIndexAdapter sphinxSliceIndexAdapter() {return new SphinxSliceIndexAdapter();}

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SemanticSearch semanticSearch(){
        return new SemanticSearchCassandraImpl();
    }
}
