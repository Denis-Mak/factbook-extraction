package it.factbook.extraction.cli;

import it.factbook.extraction.ClusterProcessor;
import it.factbook.extraction.FactsMessage;
import it.factbook.extraction.config.ConfigPropertiesStaging;
import it.factbook.search.Fact;
import it.factbook.search.repository.FactAdapter;
import it.factbook.util.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ClusterFacts {
    private static final Logger log = LoggerFactory.getLogger(ClusterFacts.class);
    private static int factProcessed                = 0;

    public static void main(String[] ars){
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(ConfigPropertiesStaging.class);
        FactAdapter factAdapter = context.getBean(FactAdapter.class);
        ClusterProcessor clusterProcessor = context.getBean(ClusterProcessor.class);
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
            List<Fact> factBatch = factAdapter.getFactBatch(i,1000);
            factsMessage.setFacts(factBatch);
            batchSize = factBatch.size();
            lastFactId = factBatch.get(batchSize - 1).getId();
            if (batchSize > 0) {
                List<Fact> factsWithClusterId = new ArrayList<>(batchSize);
                factBatch.parallelStream()
                        .forEach(fact -> {
                            boolean[] factFingerprintVector = clusterProcessor.calculateFactFingerprint(fact);
                            int clusterId = 0;

                            Fact factWithClusterId = new Fact.Builder(fact)
                                    .clusterId(clusterId)
                                    .factFingerprint(factFingerprintVector)
                                    .build();
                            factsWithClusterId.add(factWithClusterId);
                        });
                factAdapter.appendFacts(factsWithClusterId, DbUtils.StorageMode.REPLACE);
                factProcessed += batchSize;
                log.debug("Facts processed {}", factProcessed);
                log.debug("FactFingerprint calculation total timing: {}", ClusterProcessor.fingerprintCalcTiming);

                i = i + 1000;
            }
        } while (batchSize > 0);
        System.out.println("Last fact ID is " + lastFactId);

        try {
            DbUtils.enableKeys(doccacheDataSource, "Fact");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
    }
}
