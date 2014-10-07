package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.config.ConfigPropertiesTest;
import it.factbook.search.Fact;
import it.factbook.search.repository.ClusterAdapter;
import it.factbook.util.BitUtils;
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

    private static final Fact sourceFact = new Fact.Builder().golem(Golem.WIKI_RU).content("\n" +
            "\n" +
            "В одном из строящихся на территории «новой Москвы» высотных домов сорвался технический лифт, в котором находились трое рабочих. Все трое скончались на месте, упав с высоты 16 этажа.\n" +
            "\n" +
            "Трагедия в поселке Коммунарка произошла около половины девятого вечера в понедельник. На месте ЧП работает следственно-оперативная группа, передаёт Интерфакс.\n" +
            "\n" +
            "Почему рухнул лифт, и кто в этом виноват, соблюдались ли правила безопасности, выясняется.\n" +
            "\n" +
            "Увы, такие случаи – не редкость. Напомним, в начале февраля в подмосковном Дмитрове из-за неисправного лифта погибла молодая мать, двухлетний малыш остался сиротой.\n" +
            "\n" +
            "Жители злополучного дома утверждают, что лифт-убийца давно ездил буквально на одном тросе. В день трагедии его в очередной раз ремонтировали.\n").build();
    private static final Fact closeFact1 = new Fact.Builder().golem(Golem.WIKI_RU).content("Трое рабочих погибли, упав с высоты 16-го этажа в строительном лифте на территории \"новой Москвы\", сообщил \"Интерфаксу\" источник в правоохранительных органах.\n" +
            "\n" +
            "По его данным, в понедельник примерно в 20:30 в поселке Коммунарка при строительстве высотного дома произошло падение технического лифта, в котором находились трое рабочих. От полученных травм все трое скончались на месте.\n" +
            "\n" +
            "На место происшествия прибыла следственно-оперативная группа, которая установила, что лифт рухнул с 16 этажа.\n" +
            "\n" +
            "В пресс-службе московской полиции \"Интерфаксу\" подтвердили факт гибели трех человек.").build();
    private static final Fact closeFact2 = new Fact.Builder().golem(Golem.WIKI_RU).content("\n" +
            "\n" +
            "Инцидент произошел в 20:30 в поселке Коммунарка, в микрорайоне Эдальго, на 33-v участке.\n" +
            "\n" +
            "Трое рабочих, которые являются гражданами Украины, устанавливали лифт в 16-этажном доме нового жилого комплекса «Эдальго». Во время проведения монтажных работ в лифтовой шахте люлька лифта сорвалась и строители упали вниз с высоты 16-го этажа. В результате рабочие получили тяжелые травмы, не совместимые с жизнью.\n" +
            "\n" +
            "На месте происшествия работают сотрудники полиции и спасательные службы. Ведутся оперативно-следственные работы.\n").build();
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
        int dst1 = BitUtils.getHammingDistance(fingerprint, closeFingerprint1);
        int dst2 = BitUtils.getHammingDistance(fingerprint, closeFingerprint2);
        int dst25 = BitUtils.getHammingDistance(closeFingerprint1, closeFingerprint2);
        int dst3 = BitUtils.getHammingDistance(fingerprint, distantFingerprint1);
        int dst4 = BitUtils.getHammingDistance(fingerprint, distantFingerprint2);
        int dst5 = BitUtils.getHammingDistance(fingerprint, distantFingerprint3);
        assertEquals(3, dst1);
        assertEquals(5, dst2);
        assertEquals(4, dst3);
        assertEquals(2, dst4);
        assertEquals(6, dst5);
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