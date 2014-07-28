package extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ProfileMessageTest {
    private static ProfileMessage testProfileMessage = new ProfileMessage();
    static {
        testProfileMessage.setInitialQuery("Vasya");
        testProfileMessage.setQueryLines(Arrays.asList(
                Arrays.asList(
                        Arrays.asList("Line1.WG1.Word1", "Line1.WG1.Word2"),
                        Arrays.asList("Line1.WG2.Word1", "Line1.WG2.Word2")
                ),
                Arrays.asList(
                        Arrays.asList("Line2.WG1.Word1", "Line2.WG1.Word2")
                )
        ));
    }
    private static String profileMessageJson = "{\"initialQuery\":\"Vasya\"," +
            "\"queryLines\":[[[\"Line1.WG1.Word1\",\"Line1.WG1.Word2\"],[\"Line1.WG2.Word1\",\"Line1.WG2.Word2\"]]," +
            "[[\"Line2.WG1.Word1\",\"Line2.WG1.Word2\"]]]}";
    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJsonSerialization() throws Exception {
        String msgJson = mapper.writeValueAsString(testProfileMessage);
        assertEquals(profileMessageJson, msgJson);
    }

    @Test
    public void testJsonDeserialization() throws Exception {
        ProfileMessage msg = mapper.readValue(profileMessageJson, ProfileMessage.class);
        assertEquals(testProfileMessage, msg);
    }

}