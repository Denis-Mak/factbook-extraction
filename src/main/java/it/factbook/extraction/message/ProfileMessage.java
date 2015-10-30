package it.factbook.extraction.message;

import it.factbook.dictionary.WordForm;
import it.factbook.search.SearchProfile;

import java.util.List;

/**
 * Contains profile to start searching the Internet by it.
 * There are only several profile properties those that essential for searching.
 */
public class ProfileMessage {
    private long profileId;
    private String initialQuery;
    private List<List<List<WordForm>>> queryLines;

    /**
     *
     * @return ID of the profile that is request external search
     */
    public long getProfileId() {
        return profileId;
    }

    /**
     * Profile ID is used to log statistic of search requests and find source of the request
     *
     * @param profileId any long greater than 0
     */
    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }

    /**
     *
     * @return query that was entered by user
     */
    public String getInitialQuery() {
        return initialQuery;
    }

    /**
     * Query from {@link SearchProfile#getQuery()} using to add to each query to a search engine as additional filter
     *
     * @param initialQuery query that was entered by user
     */
    public void setInitialQuery(String initialQuery) {
        this.initialQuery = initialQuery;
    }

    /**
     *
     * @return words composed idioms of the profile lines
     */
    public List<List<List<WordForm>>> getQueryLines() {
        return queryLines;
    }

    /**
     *
     * @param queryLines words composed idioms of the profile lines
     */
    public void setQueryLines(List<List<List<WordForm>>> queryLines) {
        this.queryLines = queryLines;
    }

    @Override
    public int hashCode(){
        int result = 7;
        if (initialQuery != null) result = 31 * result + initialQuery.hashCode();
        for (List<List<WordForm>> line: queryLines){
            for (List<WordForm> wordgram: line){
                for (WordForm word: wordgram){
                    if (word != null) result = 31 * result + word.hashCode();
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof ProfileMessage))  return false;
        ProfileMessage pm = (ProfileMessage) obj;
        if (!pm.initialQuery.equals(this.initialQuery)) return false;
        for (int lineIdx = 0; lineIdx < queryLines.size(); lineIdx++){
            for (int wgIdx = 0; wgIdx < queryLines.get(lineIdx).size(); wgIdx++){
                for (WordForm word: queryLines.get(lineIdx).get(wgIdx)){
                    if (!pm.queryLines.get(lineIdx).get(wgIdx).contains(word)) return false;
                }
            }
        }
        return true;
    }
}
