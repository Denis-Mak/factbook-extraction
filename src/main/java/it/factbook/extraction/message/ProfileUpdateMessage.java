package it.factbook.extraction.message;

import it.factbook.search.FoundFact;
import it.factbook.search.SearchProfile;

/**
 * Contains the profile to update and the fact using to this update.
 */
public class ProfileUpdateMessage {
    private SearchProfile searchProfile;
    private FoundFact fact;

    public SearchProfile getSearchProfile() {
        return searchProfile;
    }

    public void setSearchProfile(SearchProfile searchProfile) {
        this.searchProfile = searchProfile;
    }

    public FoundFact getFact() {
        return fact;
    }

    public void setFact(FoundFact fact) {
        this.fact = fact;
    }
}
