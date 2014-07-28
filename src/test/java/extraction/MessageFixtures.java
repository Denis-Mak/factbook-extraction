package extraction;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 *
 */
public class MessageFixtures {
    public static ObjectMapper mapper = new ObjectMapper();
    public static String profileMessageJson = "{\"initialQuery\":\"iphone\","+
            "\"queryLines\":[[[\"ios\",\"issued\"]]," +
            "[[\"power\",\"consumption\"]]]}";

    public static ProfileMessage profileMessage;
    static {
        try {
            profileMessage = mapper.readValue(profileMessageJson, ProfileMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
