package extraction;

import java.util.List;

/**
 *
 */
public class SearchResultsMessage {
    public List<Link> links;

    public  SearchResultsMessage(){}

    public SearchResultsMessage(List<Link> links){
        this.links = links;
    }
}
