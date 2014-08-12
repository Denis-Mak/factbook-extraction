package it.factbook.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 *
 */
public class MessageFixtures {
    public static ObjectMapper mapper = new ObjectMapper();
    public static String profileMessageJson = "{\"initialQuery\":\"\","+
            "\"queryLines\":[" +
            "[[{\"word\":\"ios\",\"golemId\":2},{\"word\":\"iphone\",\"golemId\":2}]]," +
            "[[{\"word\":\"power\",\"golemId\":2},{\"word\":\"consumption\",\"golemId\":2}]]," +
            "[[{\"word\":\"телефон\",\"golemId\":1},{\"word\":\"эпл\",\"golemId\":1}]]]}";

    public static ProfileMessage profileMessage;
    static {
        try {
            profileMessage = mapper.readValue(profileMessageJson, ProfileMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
