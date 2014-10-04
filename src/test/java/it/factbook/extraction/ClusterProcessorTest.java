package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.config.ConfigPropertiesTest;
import it.factbook.search.Fact;
import it.factbook.search.repository.ClusterAdapter;
import it.factbook.util.BitUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

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
    @Ignore
    public void testCalculateFactFingerprint() throws Exception {
        boolean[] fingerprint = clusterProcessor.calculateFactFingerprint(sourceFact);
        boolean[] closeFingerprint1 = clusterProcessor.calculateFactFingerprint(closeFact1);
        boolean[] closeFingerprint2 = clusterProcessor.calculateFactFingerprint(closeFact2);
        boolean[] distantFingerprint1 = clusterProcessor.calculateFactFingerprint(distantFact1);
        boolean[] distantFingerprint2 = clusterProcessor.calculateFactFingerprint(distantFact2);
        boolean[] distantFingerprint3 = clusterProcessor.calculateFactFingerprint(distantFact3);
        int dst1 = BitUtils.getHammingDistance(fingerprint, closeFingerprint1);
        int dst2 = BitUtils.getHammingDistance(fingerprint, closeFingerprint2);
        int dst25 = BitUtils.getHammingDistance(closeFingerprint1, closeFingerprint2);
        int dst3 = BitUtils.getHammingDistance(fingerprint, distantFingerprint1);
        int dst4 = BitUtils.getHammingDistance(fingerprint, distantFingerprint2);
        int dst5 = BitUtils.getHammingDistance(fingerprint, distantFingerprint3);
        assertEquals(2, dst1);
        assertEquals(2, dst2);
        assertEquals(4, dst3);
        assertEquals(4, dst4);
        assertEquals(4, dst5);
    }

    @Test
    public void testCalculateFactDistance() throws Exception {
        int distanceToCloseFact1 = 0;
        int distanceToCloseFact2 = 0;
        int distanceToDistantFact1 = 0;
        int distanceToDistantFact2 = 0;
        int distanceToDistantFact3 = 0;
        distanceToCloseFact1 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), closeFact1.getContent());
        distanceToCloseFact2 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), closeFact2.getContent());
        distanceToDistantFact1 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), distantFact1.getContent());
        distanceToDistantFact2 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), distantFact2.getContent());
        distanceToDistantFact3 = clusterProcessor.calculateFactDistance(sourceFact.getContent(), distantFact3.getContent());

        assertEquals(73, distanceToCloseFact1);
        assertEquals(32, distanceToCloseFact2);
        assertEquals(14, distanceToDistantFact1);
        assertEquals(0, distanceToDistantFact2);
        assertEquals(0, distanceToDistantFact3);
    }
}