package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.config.ConfigPropertiesTest;
import it.factbook.search.Cluster;
import it.factbook.search.Fact;
import it.factbook.search.repository.ClusterAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= ConfigPropertiesTest.class, loader=AnnotationConfigContextLoader.class)
public class ClusterProcessorTest {
    @Autowired
    private ClusterProcessor clusterProcessor;

    @Autowired
    ClusterAdapter clusterAdapter;

    private static final Fact sourceFact = new Fact.Builder().golem(Golem.WIKI_RU).content("Причинами войны стали отказ России активно поддерживать континентальную блокаду").build();
    private static final Fact closeFact1 = new Fact.Builder().golem(Golem.WIKI_RU).content("Официальной причиной войны был отказ поддерживать континентальную блокаду").build();
    private static final Fact closeFact2 = new Fact.Builder().golem(Golem.WIKI_RU).content("Обострение отношений России и Франции вызванное несоблюдением Россией континентальной блокады").build();
    private static final Fact distantFact1 = new Fact.Builder().golem(Golem.WIKI_RU).content("Причина всех войн одна - эксплуатация меньшинством, в своих корыстных целях, желаний большинства").build();
    private static final Fact distantFact2 = new Fact.Builder().golem(Golem.WIKI_RU).content("Причины Крымской войны").build();
    private static final Fact distantFact3 = new Fact.Builder().golem(Golem.WIKI_RU).content("козленок кулькевич сверял ежегодники землеустроительные молхо приготовьтесь наказанный отмыванием").build();

    @Test
    public void testCalculateFactFingerprint() throws Exception {
        boolean[] fingerprint = clusterProcessor.calculateFactFingerprint(sourceFact);
        boolean[] closeFingerprint1 = clusterProcessor.calculateFactFingerprint(closeFact1);
        boolean[] closeFingerprint2 = clusterProcessor.calculateFactFingerprint(closeFact2);
        boolean[] distantFingerprint1 = clusterProcessor.calculateFactFingerprint(distantFact1);
        boolean[] distantFingerprint2 = clusterProcessor.calculateFactFingerprint(distantFact2);
        boolean[] distantFingerprint3 = clusterProcessor.calculateFactFingerprint(distantFact3);
        int dst1 = ClusterProcessor.getHammingDistance(fingerprint, closeFingerprint1);
        int dst2 = ClusterProcessor.getHammingDistance(fingerprint, closeFingerprint2);
        int dst25 = ClusterProcessor.getHammingDistance(closeFingerprint1, closeFingerprint2);
        int dst3 = ClusterProcessor.getHammingDistance(fingerprint, distantFingerprint1);
        int dst4 = ClusterProcessor.getHammingDistance(fingerprint, distantFingerprint2);
        int dst5 = ClusterProcessor.getHammingDistance(fingerprint, distantFingerprint3);
        assertEquals(2, dst1);
        assertEquals(2, dst2);
        assertEquals(4, dst3);
        assertEquals(4, dst4);
        assertEquals(4, dst5);
    }

    @Test
    public void testCalculateFactDistance() throws Exception {
        int distanceToCloseFact1 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), closeFact1.getContent());
        int distanceToCloseFact2 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), closeFact2.getContent());
        int distanceToCloseFact25 = clusterProcessor.calculateFactDistance(closeFact1.getContent(), closeFact2.getContent());
        int distanceToDistantFact1 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), distantFact1.getContent());
        int distanceToDistantFact2 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), distantFact2.getContent());
        int distanceToDistantFact3 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), distantFact3.getContent());

        assertEquals(36, distanceToCloseFact1);
        assertEquals(16, distanceToCloseFact2);
        assertEquals(7, distanceToDistantFact1);
        assertEquals(12, distanceToDistantFact2);
        assertEquals(0, distanceToDistantFact3);
    }

    @Test
    public void testConvertToInt(){
        boolean[] bits = {true,true};
        long res = ClusterProcessor.convertToInt(bits);
        assertEquals(3L, res);

        boolean[] bits1 = {true,true,false};
        res = ClusterProcessor.convertToInt(bits1);
        assertEquals(3L, res);
    }

    @Test
    public void testGetVectorsOnDistance(){
        boolean[][] res = ClusterProcessor.getVectorsOnDistance(new boolean[]{true,true,false,true}, 3);

        assertEquals("0011", ClusterProcessor.bitsToString(res[0]));
        assertEquals("0000", ClusterProcessor.bitsToString(res[1]));
        assertEquals("0110", ClusterProcessor.bitsToString(res[2]));
        assertEquals("1010", ClusterProcessor.bitsToString(res[3]));

        res = ClusterProcessor.getVectorsOnDistance(new boolean[]{true,true,false,true}, 1);
        assertEquals("0101", ClusterProcessor.bitsToString(res[0]));
        assertEquals("1001", ClusterProcessor.bitsToString(res[1]));
        assertEquals("1111", ClusterProcessor.bitsToString(res[2]));
        assertEquals("1100", ClusterProcessor.bitsToString(res[3]));

        boolean[][] res = ClusterProcessor.getVectorsOnDistance(new boolean[]{true,false,false,false}, 2);
        assertEquals("0001", ClusterProcessor.bitsToString(res[0]));
        assertEquals("0111", ClusterProcessor.bitsToString(res[1]));
        assertEquals("0100", ClusterProcessor.bitsToString(res[2]));
        assertEquals("1011", ClusterProcessor.bitsToString(res[3]));
        assertEquals("1000", ClusterProcessor.bitsToString(res[4]));
        assertEquals("1110", ClusterProcessor.bitsToString(res[5]));
    }

    @Test
    public void testGetCloseFactsFingerprints(){
        boolean[] senseVector = clusterProcessor.calculateFactFingerprint(
                new Fact.Builder().golem(Golem.WIKI_EN).content("The archipelago's beaches, climate and important natural attractions, especially Maspalomas in Gran Canaria and Teide National Park and Mount Teide (the third tallest volcano in the world measured from its base on the ocean floor), make it a major tourist destination with over 12 million visitors per year, especially Tenerife, Gran Canaria and Lanzarote.")
                .build()
        );
        List<Integer> closeClustersId = ClusterProcessor.getCloseFactsFingerprints(senseVector);
        assertEquals(435, closeClustersId.size());

        List<Cluster> realClustersCount = clusterAdapter.getByFactFingerprint(closeClustersId);
        assertEquals(435, realClustersCount.size());
    }
}