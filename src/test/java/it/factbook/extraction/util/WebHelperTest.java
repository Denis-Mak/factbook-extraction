package it.factbook.extraction.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebHelperTest {

    @Test
    public void testGetDecodedURL() throws Exception {
        String rawUrl = "https://www.scribd.com/doc/77147329/Английский-язык-Учебник-устного-перевода";
        String utf8EncodedUrl = "/2012/05/17/%D0%BF-%D0%B4%D0%B6-%D0%B1%D1%8C%D1%8E%D0%BA%D0%B5%D0%BD%D0%B5%D0%BD/";
        String win1251EncodedUrl = "http://www.lib.unn.ru/php/catalog.php?Index=1&Letter=%CF&DB=1";

        assertEquals("https://www.scribd.com/doc/77147329/Английский-язык-Учебник-устного-перевода", WebHelper.getDecodedURL(rawUrl));
        assertEquals("/2012/05/17/п-дж-бьюкенен/", WebHelper.getDecodedURL(utf8EncodedUrl));
        assertEquals("http://www.lib.unn.ru/php/catalog.php?Index=1&Letter=%CF&DB=1", WebHelper.getDecodedURL(win1251EncodedUrl));
    }
}