package it.factbook.extraction.client;

import it.factbook.extraction.MessageFixtures;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ClientTestUtils {
    public static void testGetQueries(SearchEngineClient searchEngineClient){
        List<Query> queries = searchEngineClient.getQueries(MessageFixtures.profileMessage);
        assertEquals(2, queries.size());
        assertEquals("mobile ((телефон | эпл))", queries.get(0).query);
        assertEquals("mobile ((ios | iphone) | (power | consumption))", queries.get(1).query);
    }

    public static void testGetQueryForProfileWithoutLines(SearchEngineClient searchEngineClient){
        List<Query> queries = searchEngineClient.getQueries(MessageFixtures.profileMessageWithoutLines);
        assertEquals(1, queries.size());
        assertEquals("maduro maio", queries.get(0).query);
    }
}
