package it.factbook.extraction.client;

import it.factbook.extraction.message.ProfileMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import java.util.List;

/**
 *
 */
public interface SearchEngineClient extends MessageListener{

    public void onMessage(Message message);

    public List<Query> getQueries(ProfileMessage msg);
}
