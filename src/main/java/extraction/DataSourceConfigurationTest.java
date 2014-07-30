package extraction;

import com.jolbox.bonecp.BoneCPDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 *
 */
@Configuration
public class DataSourceConfigurationTest {
    private static final String PROPERTY_NAME_DATABASE_DRIVER = "com.mysql.jdbc.Driver";
    private static final String PROPERTY_NAME_DATABASE_PASSWORD = "";
    private static final String PROPERTY_NAME_DATABASE_URL = "jdbc:mysql://localhost/test?characterEncoding=UTF-8&useUnicode=true";
    private static final String PROPERTY_NAME_DATABASE_USERNAME = "root";

    @Bean
    public DataSource dataSource() {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass(PROPERTY_NAME_DATABASE_DRIVER);
        dataSource.setJdbcUrl(PROPERTY_NAME_DATABASE_URL);
        dataSource.setUsername(PROPERTY_NAME_DATABASE_USERNAME);
        dataSource.setPassword(PROPERTY_NAME_DATABASE_PASSWORD);

        return dataSource;
    }
}
