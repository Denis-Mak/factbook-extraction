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
import java.util.*;

/**
 *
 */
@org.springframework.stereotype.Component
public class ClusterProcessor implements MessageListener {
    private static ObjectMapper jsonMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(ClusterProcessor.class);

    private static long roughClusterCalcTiming      = 0;
    private static long getWordFormsTiming          = 0;
    private static long findCloseClustersTiming     = 0;
    private static long calculateClusterIdTiming    = 0;
    private static long createClustersTiming        = 0;
    private static long updateFactsTiming           = 0;

    private final static int DISTANCE_THRESHOLD = 25;
    private final static int COMPONENTS_IN_FINGERPRINT = 5;
    private final static Map<Integer,List<Integer>> POSSIBLE_COMBINATIONS = new HashMap<>(3);
    static {
        POSSIBLE_COMBINATIONS.put(20, Arrays.asList(1,20,190,1140));
        POSSIBLE_COMBINATIONS.put(25, Arrays.asList(1,25,300,2300));
        POSSIBLE_COMBINATIONS.put(30, Arrays.asList(1,30,435,4060));
    }

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
            List<Fact> factsWithClusterId = new ArrayList<>(msg.getFacts().size());
            msg.getFacts().parallelStream()
                    .forEach(fact -> {
                        boolean[] factFingerprintVector = calculateFactFingerprint(fact);
                        int factFingerprintKey = 0;
                        int clusterId = 0;
                        if (factFingerprintVector.length != 0) {
                            factFingerprintKey = convertToInt(factFingerprintVector);
                            clusterId = 0;
                            long findingStartTime = System.currentTimeMillis();
                            List<Cluster> clusters = getClusters(getCloseFactsFingerprints(factFingerprintVector));
                            ClusterProcessor.findCloseClustersTiming += System.currentTimeMillis() - findingStartTime;
                            if (clusters.size() == 0) {
                                clusterId = createNewCluster(fact, factFingerprintKey);
                            } else {
                                clusterId = calculateClusterId(fact, clusters);
                                if (clusterId == 0) {
                                    long startTime = System.currentTimeMillis();
                                    clusterId = createNewCluster(fact, factFingerprintKey);
                                    ClusterProcessor.createClustersTiming += System.currentTimeMillis() - startTime;
                                }
                            }
                        }
                        long startTime = System.currentTimeMillis();
                        Fact factWithClusterId = new Fact.Builder(fact)
                                .clusterId(clusterId)
                                .factFingerprint(factFingerprintKey)
                                .build();
                        factsWithClusterId.add(factWithClusterId);
                        ClusterProcessor.updateFactsTiming += System.currentTimeMillis() - startTime;
                    });
            factAdapter.appendFacts(factsWithClusterId, DbUtils.StorageMode.REPLACE);
            log.debug("RoughClusterId calculation total timing: {}", ClusterProcessor.roughClusterCalcTiming);
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

    boolean[] calculateFactFingerprint(Fact fact){
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
        ClusterProcessor.roughClusterCalcTiming += System.currentTimeMillis() - startTime;
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
        ClusterProcessor.calculateClusterIdTiming += System.currentTimeMillis() - startTime;
        return clusterId;
    }

    int calculateFactDistance(String from, String to){
        String strToCompareA = StringUtils.leftOnlyLetters(from).toLowerCase();
        String strToCompareB = StringUtils.leftOnlyLetters(to).toLowerCase();
        int lenA = strToCompareA.length();
        int lenB = strToCompareB.length();
        if (Math.abs(lenA - lenB) / (float)(lenA + lenB) > 0.33){
            return 100;
        }
        String lcs = StringUtils.allCommonSubstrings(strToCompareA, strToCompareB);
        int sumLen = strToCompareA.length() + strToCompareB.length();
        return Math.round(lcs.length() / (float)sumLen * 100);
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
        final int DISTANCE = 2;
        List<Integer> fingerprints = new ArrayList<>(POSSIBLE_COMBINATIONS.get(Stem.SENSE_VECTOR_LENGTH).get(DISTANCE));
        boolean[][] closeFingerprintsBool = getVectorsOnDistance(factFingerPrint, DISTANCE);
        for(boolean[] each: closeFingerprintsBool){
            fingerprints.add(convertToInt(each));
        }
        return fingerprints;
    }

    public static boolean[][] getVectorsOnDistance(final boolean[] bits, final int distance){
        boolean[][] result = new boolean[POSSIBLE_COMBINATIONS.get(Stem.SENSE_VECTOR_LENGTH).get(distance)][bits.length];
        boolean[] modified = new boolean[bits.length];
        System.arraycopy(bits, 0, modified, 0, bits.length);
        allCombination(bits, 0, distance, modified, result, 0);
        return result;
    }

    public static int allCombination(final boolean[] bits, int start, int r, boolean[] modified, boolean[][] result, int resCount) {
        int length = bits.length;
        if (r == 1) {
            for (int i = start; i < length; i++) {
                modified[i] = !bits[i];
                System.arraycopy(modified, 0, result[resCount], 0, modified.length);
                resCount++;
                // swap bit back, because it's cheaper than copy unmodified array
                modified[i] = bits[i];
            }

        } else {
            for (int k = start; k < length - r + 1; k++) {
                modified[k] = !bits[k];
                resCount = allCombination(bits, k + 1, r - 1, modified, result, resCount);
                // swap bit back, because it's cheaper than copy unmodified array
                modified[k] = bits[k];
            }
        }
        return resCount;
    }

    public static int getHammingDistance(boolean[] vec1, boolean[] vec2){
        if (vec1.length != vec2.length){
            throw new IllegalArgumentException("Vectors must be the same size");
        }
        int distance = 0;
        for(int i = 0; i < vec1.length; i++){
            if(vec1[i] != vec2[i]){
                distance++;
            }
        }
        return distance;
    }

    public static int convertToInt(final boolean[] bits) {
        if (bits.length > 32) {
            throw new IllegalArgumentException("Vector is too long. Vector size for int must be less than 32.");
        }
        int value = 0;
        for (int i = 0; i < bits.length; ++i) {
            value += bits[i] ? (1 << i) : 0;
        }
        return value;
    }

    public static String bitsToString(boolean[] bits){
        String s = "";
        for (boolean each:bits){
            s += (each) ? "1" : "0";
        }

        return s;
    }

}
