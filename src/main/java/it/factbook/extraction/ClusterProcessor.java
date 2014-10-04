package it.factbook.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.factbook.dictionary.Stem;
import it.factbook.dictionary.repository.StemAdapter;
import it.factbook.dictionary.repository.WordFormAdapter;
import it.factbook.search.Cluster;
import it.factbook.search.Fact;
import it.factbook.search.repository.ClusterAdapter;
import it.factbook.search.repository.FactAdapter;
import it.factbook.util.BitUtils;
import it.factbook.util.DbUtils;
import it.factbook.util.StringUtils;
import it.factbook.util.TextSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@org.springframework.stereotype.Component
public class ClusterProcessor implements MessageListener {
    private static ObjectMapper jsonMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(ClusterProcessor.class);

    public static long fingerprintCalcTiming = 0;
    private static long getWordFormsTiming          = 0;
    private static long findCloseClustersTiming     = 0;
    private static long calculateClusterIdTiming    = 0;
    private static long createClustersTiming        = 0;
    private static long updateFactsTiming           = 0;
    private static int factProcessed                = 0;

    private final static int DISTANCE_THRESHOLD = 65;
    private final static int COMPONENTS_IN_FINGERPRINT = 5;

    private static class Component {
        int pos;
        double probability;
    }

    @Autowired
    StemAdapter stemAdapter;

    @Autowired
    WordFormAdapter wordFormAdapter;

    @Autowired
    ClusterAdapter clusterAdapter;

    @Autowired
    FactAdapter factAdapter;

    @Autowired
    private TextSplitter textSplitter;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public void onMessage(Message message) {
        try {
            jsonMapper.registerModule(new JodaModule());
            FactsMessage msg = jsonMapper.readValue(message.getBody(), FactsMessage.class);
            if (msg.getFacts().size() < 1) {
                return;
            }
            log.debug("Received message. Facts count: {}", msg.getFacts().size());
            int batchSize = msg.getFacts().size();
            List<Fact> factsWithClusterId = new ArrayList<>(batchSize);
            msg.getFacts().parallelStream()
                    .forEach(fact -> {
                        boolean[] factFingerprintVector = calculateFactFingerprint(fact);
                        int factFingerprintKey = 0;
                        int clusterId = 0;

                        long startTime = System.currentTimeMillis();
                        Fact factWithClusterId = new Fact.Builder(fact)
                                .clusterId(clusterId)
                                .factFingerprint(factFingerprintVector)
                                .build();
                        factsWithClusterId.add(factWithClusterId);
                        ClusterProcessor.updateFactsTiming += System.currentTimeMillis() - startTime;
                    });
            factAdapter.appendFacts(factsWithClusterId, DbUtils.StorageMode.REPLACE);
            factProcessed += batchSize;
            log.debug("Facts processed {}", factProcessed);
            log.debug("RoughClusterId calculation total timing: {}", ClusterProcessor.fingerprintCalcTiming);
            log.debug("Get WordForms total timing: {}", ClusterProcessor.getWordFormsTiming);
            log.debug("Finding close clusters total timing: {}", ClusterProcessor.findCloseClustersTiming);
            log.debug("Calculate clusterId total timing: {}", ClusterProcessor.calculateClusterIdTiming);
            log.debug("Create clusters total timing: {}", ClusterProcessor.createClustersTiming);
            log.debug("Update total timing: {}", ClusterProcessor.updateFactsTiming);
            /*FactsMessage factsMessage = new FactsMessage();
            factsMessage.setFacts(factsWithClusterId);
            passFactsToIndexUpdater(factsMessage);*/
        } catch (IOException e) {
            log.error("Error during unpack DocumentMessage: {}", e);
        }
    }

    public boolean[] calculateFactFingerprint(Fact fact){
        long startTime = System.currentTimeMillis();
        List<Integer> stemIdsOfFact = wordFormAdapter.getStemIds(fact.getGolem(), textSplitter.splitWords(fact.getContent()));
        ClusterProcessor.getWordFormsTiming += System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        Component[] sumOfSenseVectors = new Component[Stem.SENSE_VECTOR_LENGTH];
        int notNullStems = 0;
        for (Integer stemId: stemIdsOfFact){
            if (stemId != null){
                double[] stemSenseVector = stemAdapter.getSenseVector(fact.getGolem(), stemId);
                ClusterProcessor.sumVectors(sumOfSenseVectors, stemSenseVector);
                notNullStems++;
            }
        }
        if (notNullStems == 0) {
            return new boolean[0];
        }
        Arrays.sort(sumOfSenseVectors, (e1, e2) -> Double.compare(e2.probability, e1.probability));
        List<Integer> componentsOfFingerprint = new ArrayList<>(COMPONENTS_IN_FINGERPRINT);
        for (int i = 0; i < COMPONENTS_IN_FINGERPRINT; i++){
            componentsOfFingerprint.add(sumOfSenseVectors[i].pos);
        }
        boolean[] factFingerprint = new boolean[Stem.SENSE_VECTOR_LENGTH];
        for (int i = 0; i < Stem.SENSE_VECTOR_LENGTH; i++){
            if (componentsOfFingerprint.contains(i)){
                factFingerprint[i] = true;
            }
        }
        ClusterProcessor.fingerprintCalcTiming += System.currentTimeMillis() - startTime;
        return factFingerprint;
    }

    private static void sumVectors(Component[] a, double[] b) {
        for (int i = 0; i < b.length; i++){
            Component c   = new Component();
            c.pos         = i;
            if (a[i] != null) {
                c.probability = a[i].probability + b[i];
            } else {
                c.probability = b[i];
            }
            a[i] = c;
        }
    }

    List<Cluster> getClusters(List<Integer> closeFingerprints){
        return clusterAdapter.getByFactFingerprint(closeFingerprints);
    }

    int calculateClusterId(Fact fact, List<Cluster> closeClusters){
        int maxDist = 0;
        int clusterId = 0;
        long startTime = System.currentTimeMillis();
        for (Cluster cluster: closeClusters){
            int dist = calculateFactDistance(fact.getContent(), cluster.getSampleFact());
            if (dist > maxDist){
                maxDist = dist;
                if (maxDist >= DISTANCE_THRESHOLD){
                    clusterId = cluster.getClusterId();
                }
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        ClusterProcessor.calculateClusterIdTiming += duration;
        return clusterId;
    }

    int calculateFactDistance(String from, String to){
        String strToCompareA = StringUtils.leftOnlyLetters(from).toLowerCase();
        String strToCompareB = StringUtils.leftOnlyLetters(to).toLowerCase();
        int lenA = strToCompareA.length();
        int lenB = strToCompareB.length();
        if (Math.abs(lenA - lenB) / (float)(lenA + lenB) > 0.33){
            return 0;
        }
        int lcs = StringUtils.allCommonSubstringLength(strToCompareA, strToCompareB);
        return Math.round((2 * lcs) / (float)(lenA + lenB) * 100);
    }

    int createNewCluster(Fact fact, int factFingerprint){
        return clusterAdapter.save(new Cluster.Builder()
                .sampleFactFingerprint(factFingerprint)
                .sampleFact(fact.getContent())
                .sampleFactId(fact.getId())
                .build());
    }

    private void passFactsToIndexUpdater(FactsMessage factsMessage) {
        try {
            jsonMapper.registerModule(new JodaModule());
            String json = jsonMapper.writeValueAsString(factsMessage);
            //amqpTemplate.convertAndSend(AmqpConfig.indexUpdaterExchange().getName(), "#", json);
        } catch (JsonProcessingException e) {
            log.error("Error converting FactMessage: {}", e);
        }
    }

    public static List<Integer> getCloseFactsFingerprints(boolean[] factFingerPrint){
        int qtyOfPossibleCombinations = (Stem.SENSE_VECTOR_LENGTH - COMPONENTS_IN_FINGERPRINT) * COMPONENTS_IN_FINGERPRINT;
        List<Integer> fingerprints = new ArrayList<>(qtyOfPossibleCombinations);
        // Add fingerprint itself
        fingerprints.add(BitUtils.convertToInt(factFingerPrint));
        // Add fingerprints of vectors on Hamming distance 2
        boolean[][] closeFingerprintsBool = BitUtils.getVectorsOnDistance(factFingerPrint, qtyOfPossibleCombinations);
        for(boolean[] each: closeFingerprintsBool){
            fingerprints.add(BitUtils.convertToInt(each));
        }
        return fingerprints;
    }
}
