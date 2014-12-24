package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.config.ConfigPropertiesTest;
import it.factbook.extraction.message.DocumentMessage;
import it.factbook.search.Fact;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=ConfigPropertiesTest.class, loader=AnnotationConfigContextLoader.class)
public class FactSaverTest {
    private static final DocumentMessage documentMessage = new DocumentMessage();
    static {
        documentMessage.setGolem(Golem.WIKI_RU);
        documentMessage.setTitle("ЧАСТЬ ЗАПАДНАЯ ЕВРОПА И СЕВЕРНАЯ АМЕРИКА: ОСОБЕННОСТИ РАЗВИТИЯ");
        documentMessage.setUrl("http://uchebnikionline.com/istoria/novitnya_istoriya_krayin_zahidnoyi_yevropi_ta_pivnichnoyi_ameriki_-_baran_3a/zahidna_yevropa_pivnichna_amerika_osoblivosti_rozvitku.htm");
        documentMessage.setContent("ЧАСТЬ ЗАПАДНАЯ ЕВРОПА И СЕВЕРНАЯ АМЕРИКА: ОСОБЕННОСТИ РАЗВИТИЯ\n" +
                "Глава 1 Международные отношения\n" +
                "1 Формирование послевоенного международного порядка\n" +
                "Послевоенная политическая карта\n" +
                "Великие войны в мировой истории становились переломным пунктами в жизни народов Первая мировая война, которая закончилась поражением государств центрального лагеря, вызвала невиданные до того далеко идущие политические изм мины в Европе Вторая мировая война как первый действительно глобальный конфликт с точки зрения влияния на дальнейшую судьбу человечества не имеет исторических аналогий Она решительно сменила геополитическую ситуацию во Всемирную ьому масштабабі.\n" +
                "Первым непосредственным следствием наступления мира было определение послевоенной государственно-политической структуры и линии границ, разрушенных в результате боевых действий Взгляд на политическую карту Европы приводит к в выводу, что территориальные изменения были здесь частично больше чем после Первой мировой войны Возродились существующие в довоенное время (до 1938 г) государства Возрождение европейских национальных государств пи сле национал-социалистического и фашистского господства было, конечно, значимым событием Особенно это касается Франции, одной из великих исторических стран континентненту.\n" +
                "Однако не все европейские народы смогли восстановить своего государственность Народы Литвы, Латвии и Эстонии вновь оказались под господством СССР Судьбу разгромленной в войне Германии должны были решать государства-перем можници, которые оккупировали ее территорию Великих изменения претерпели государственные очертания Польше: ее территория по сравнению с установленными в Версале границами, сместилась далеко на Захихід.\n" +
                "Итог Второй мировой войны, с точки зрения территориальных изменений, был полезным для Советского Союза Он, наряду с тремя балтийскими странами, аннексировал северную половину Восточной Пруссии, занятий объявлений на севере Финляндии часть Карельского перешейка и район Петсамо В СССР были присоединены также отняты у Румынии Бессарабия и северная часть Буковины, Закарпатская Украина, которая входила в мижвое нный время в состав Чехословакии, и оккупирована Красной Армией в 1939 г и утрачена в начале немецко-советской войны Западная Украина Произошли и другие территориальные изменения Франция, конечно, верну а себе Эльзас-Лотарингию, часть Юлийские Крайны отошла к Югославии Румыния вынуждена была передать Болгарии Добруджу Наряду с этим были проведены менее значительные коррекции линии границ, и на более длительный ч ас оставался открытым вопрос о будущем важной немецкой промышленной области - Саар- Саару.\n" +
                "Оформление государственно-политического статуса Германии затянулось Союзники так и не смогли достичь единства, поэтому здесь не вступил ни один договор Как было условлено еще до окончания войны, Германию и А Австрию, поделили на зоны оккупации Советы контролировали восточные земли Германии - Саксонию, Тюрингию, Бранденбург и Мекленбург; британцы управляли Северной Германией, Вестфалии и частью Рейнской о бластов; южнее расположились американская и французская зоны оккупации Берлин, находился в советской зоне оккупации, был разделен на четыре сектора Власть в стране осуществляла Союзная контрольная совет (СКС), в состав которой входили главнокомандующие оккупационных войск держав-победительницжниць.\n" +
                "Большие изменения после капитуляции Японии произошли на Дальнем Востоке Вся территория Японии, за исключением занятых Советским Союзом Курильских островов, была оккупирована войсками США при определенной участия Отделения илов Британского содружества В отличие от Германии в Японии не дошло до разделения на зоны оккупации Императорское правительство, несмотря на ограничения компетенции, продолжал функционировать в дальнейшем, а решение ство Рено в Токио Союзного Совета (США, СССР, Британское Содружество, Китай) не были обязательными для американского главнокомандующего генерала Дугласа Макартура, который занимал огромную, почти неограниченную, власть на островавах.\n" +
                "Согласно с постановлениями Каирской (1943) и Ялтинской (1945) конференций союзников в Японии отобрали многочисленные территории, захваченные ею до и во время Первой мировой войны (Корея, Курильские острова, Ю энное часть Сахалина, Каролинский и Марианнський архипелаги, острова Маршалловы и Палау), накануне и во время Второй мировой войны (Маньчжурия, Внешняя Монголия.\n" +
                "Между тем обострилась внутриполитическая ситуация в Китае После провала переговоров между проводниками партии Гоминьдан (Чан Кайши) и Коммунистической партии (Мао Цзэдун и Чжоу Эньлай), посредником на я которых выступал американский генерал Джордж Маршалл, в стране возобновилась гражданская война, прервана в 1937 г в связи с японской оккупацией В июле 1946 г армия Гоминьдана начала наступление, ликвид вуючы коммунистические базы в средней и северной части Китая В ходе войны Гоминьдан, столкнувшись с огромными финансовыми трудностями и все бильщою деморализацией своих войск, в конце потерпел пора зкы 31 января 1949 г коммунистические войска заняли Пекин 1 октября 1949 г Мао Цзэдун, как председатель Государственного Совета, провозгласил в Пекине создание Китайской Народной Республики Чан Кайши с остатками армии и многотысячной массой беженцев эвакуировался на остров Тайвань США, которые предоставляли Гоминьдана большую финансовую и материальную помощь, понесли тяжелую неудачу, потеряв своего главного союзника в Азии Появ а на Дальнем Востоке коммунистического колосса вызвала на Западе гнетущее впечатлениеюче враження.\n" +
                "Параллельно с событиями, которые решали судьбу Китая, появилась корейский проблема После освобождения Кореи из-под японской оккупации союзники поделили ее территорию вдоль 38-й параллели, разграничив, тем или ином, сферы действий своих армий Южную часть со столицей в Сеуле заняли войска США, северную часть - Красная Армия Переговоры в деле объединения Кореи, которые велись с декабря 1945 под эгидой ООН, парализовал своей позицией Кремль, отстаивая мнение, что решение корейского вопроса - это дело только великих держав В этой ситуации выборы под наблюдением комиссии ООН состоялись 10 мая 1 948 г только на юге страны, а О ОН признала созданную в это время власть \\\"единственным национальным правительством объединенной Кореи\\\" Президентом Республики Корея стал Ли Сын Ман, бывший японский узник и премьер е миграционного правительства 1919ного уряду 1919 р.\n" +
                "На севере Кореи, при поддержке Советского Союза, к власти пришел малоизвестный до того Ким Ир Сен, капитан Красной Армии 2 сентября 1948 г Ким Ир Сен провозгласил образование Корейской Народно-демократический ческой Республики, правительство которой возглавил В декабре 1948 г советские войска были выведены из Кореореї.\n" +
                "Под влиянием Второй мировой войны наметились изменения ситуации на Ближнем Востоке На момент окончания войны на территории арабского Востока существовало шесть независимых государств Четыре из них были монархиями: ЕГ гипет, Ирак, Саудовская Аравия и Йемен; республиканский строй имели Сирия и Ливан В 1946 г независимость получило Королевство Трансиордании, до того - формально независимый эмират под британским контрол ем Остальные территории находилась под непосредственным влиянием Великобритании как мандат, колония или протекторат (Палестина, Кипр, Аден, Оман, шейханаты над Персидским заливомокою).\n" +
                "Вдруг, с окончанием Второй мировой войны, в Палестине вновь вспыхнул внутренний конфликт между арабским населением и все многочисленным, благодаря иммиграции, еврейским обществом и британскими м мандатной властями конце, учитывая обострение арабо-еврейской борьбы и антибританской выступлений, Великобритания передала 14 февраля 1947 г дело Палестины ООН 29 ноября 1947 Зага льна Сессия ООН большинством голосов, при активной поддержке делегации США, принял решение об образовании двух государств: еврейского и арабского 15 мая 1948 года, после окончательного завершения действия британско го мандата и вывода английских войск, Еврейская Национальный Совет, переименована на Временную Государственный Совет, провозгласила образование государства Израильзраїль.\n" +
                " \n");
    }

    private static final DocumentMessage documentMessageWithEmptyLines = new DocumentMessage();
    static {
        documentMessageWithEmptyLines.setGolem(Golem.WIKI_RU);
        documentMessageWithEmptyLines.setTitle("Умка.");
        documentMessageWithEmptyLines.setUrl("http://torrentom.com/multfilm/Russkie-multfilmi/online-umka-umka-ischet-druga-dvdrip.htm");
        documentMessageWithEmptyLines.setContent("   \n" +
                "Умка. Умка ищет друга смотреть онлайн бесплатно\n" +
                "Дорогие пользователи! Мы внедрили новую технологию онлайн проигрывания торрент файлов в оригинальном качестве. Вы можете Умка. Умка ищет друга онлайн смотреть бесплатно в хорошем качестве без регистрации, даже Full HD, все зависит от скорости вашего интернета и количества раздающих. Для реализации данной функции нужно установить ACE Stream - аналог торрент клиента для просмотра торрентов онлайн. Читать инструкцию\n" +
                "Мультфильмы\n");
    }

    @Autowired
    private FactSaver factSaver;

    @Test
    public void testRemoveHyphens() throws Exception {
        String withoutHyphens = "Тем самым с повестки дня снимался вопрос об послевоенной объединении Германии";
        String withHyphens = "Кроме того, средст¬ва в основном расходуются на фундаментальные ис¬следования.";

        assertEquals("Тем самым с повестки дня снимался вопрос об послевоенной объединении Германии", FactSaver.removeHyphens(withoutHyphens));
        assertEquals("Кроме того, средства в основном расходуются на фундаментальные исследования.", FactSaver.removeHyphens(withHyphens));
    }

    @Test
    public void testBuildDocHeader(){
        Fact docHeader = factSaver.buildDocHeader(documentMessage);
        assertEquals("1373 875 1415 2359 1583 1415 1949 582", docHeader.getTitleSense());
        assertEquals("3012755c03eae1d9227f1e666fe0d2a80df11967", docHeader.getTitleHash());
    }

    @Test
    public void testBuildListOfFact(){
        List<Fact> facts = factSaver.buildListOfFacts(1, documentMessage);
        assertEquals(11, facts.size());

        facts = factSaver.buildListOfFacts(2, documentMessageWithEmptyLines);
        assertEquals(9, facts.size());
    }
}