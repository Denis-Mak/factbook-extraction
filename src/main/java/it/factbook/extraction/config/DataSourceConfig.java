package it.factbook.extraction.config;

import com.jolbox.bonecp.BoneCPDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 *
 */
@Configuration
public class DataSourceConfig {
    @Value("${jdbc.doccache.url}")
    private String jdbcDoccacheUrl;

    @Value("${jdbc.doccache.username}")
    private String jdbcDoccacheUsername;

    @Value("${jdbc.doccache.password}")
    private String jdbcDoccachePassword;

    @Value("${jdbc.it.factbook.extraction.url}")
    private String jdbcExtractionUrl;

    @Value("${jdbc.it.factbook.extraction.username}")
    private String jdbcExtractionUsername;

    @Value("${jdbc.it.factbook.extraction.password}")
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
}
