package extraction;

import config.DataSourceConfigurationStaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 *
 */
public class Start {
    private static final Logger log = LoggerFactory.getLogger(Start.class);

    public static void main (String[] ars) throws Exception{
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DataSourceConfigurationStaging.class);
        log.info("All listners, exchanges and queues raised and ready.");
    }
}
