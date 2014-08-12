package it.factbook.extraction;

import it.factbook.dictionary.WordForm;

import java.util.List;

/**
 *
 */
public class ProfileMessage {
    private long profileId;
    private String initialQuery;
    private List<List<List<WordForm>>> queryLines;

    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }

    public String getInitialQuery() {
        return initialQuery;
    }

    public void setInitialQuery(String initialQuery) {
        this.initialQuery = initialQuery;
    }

    public List<List<List<WordForm>>> getQueryLines() {
        return queryLines;
    }

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
