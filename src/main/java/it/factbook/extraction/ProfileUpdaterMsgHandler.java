package it.factbook.extraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import it.factbook.extraction.message.ProfileUpdateMessage;
import it.factbook.search.SearchProfile;
import it.factbook.search.SearchProfileUpdater;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
public class ProfileUpdaterMsgHandler {
    private static ObjectMapper jsonMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(ProfileUpdaterMsgHandler.class);

    @Autowired
    private SearchProfileUpdater profileUpdater;

    public String handleMessage(String message) {
        String jsonToReturn = "";
        try {
            jsonMapper.registerModule(new JodaModule());
            ProfileUpdateMessage msg = jsonMapper.readValue(message, ProfileUpdateMessage.class);
            SearchProfile newProfile =
                    new SearchProfile.Builder(profileUpdater.update(msg.getSearchProfile(), msg.getFact()))
                            .updated(new DateTime())
                            .kept(msg.getSearchProfile().getKept() + 1)
                            .build();
            jsonToReturn = jsonMapper.writeValueAsString(newProfile);
        } catch (IOException e) {
            log.error("Error during unpack ProfileUpdateMessage: {}", e);
        }
        return jsonToReturn;
    }
}
