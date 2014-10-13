package it.factbook.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.factbook.extraction.message.ProfileMessage;

import java.io.IOException;

/**
 *
 */
public class MessageFixtures {
    public static ObjectMapper mapper = new ObjectMapper();
    public static String profileMessageJson = "{\"profileId\":0, \"initialQuery\":\"\","+
            "\"queryLines\":[" +
            "[[{\"word\":\"ios\",\"golem\":\"WIKI_EN\"},{\"word\":\"iphone\",\"golem\":\"WIKI_EN\"}]]," +
            "[[{\"word\":\"power\",\"golem\":\"WIKI_EN\"},{\"word\":\"consumption\",\"golem\":\"WIKI_EN\"}]]," +
            "[[{\"word\":\"телефон\",\"golem\":\"WIKI_RU\"},{\"word\":\"эпл\",\"golem\":\"WIKI_RU\"}]]]}";

    public static ProfileMessage profileMessage;
    static {
        try {
            profileMessage = mapper.readValue(profileMessageJson, ProfileMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
