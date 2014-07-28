package extraction;

import java.util.List;

/**
 *
 */
public class ProfileMessage {
    private String initialQuery;
    private List<List<List<String>>> queryLines;

    public String getInitialQuery() {
        return initialQuery;
    }

    public void setInitialQuery(String initialQuery) {
        this.initialQuery = initialQuery;
    }

    public List<List<List<String>>> getQueryLines() {
        return queryLines;
    }

    public void setQueryLines(List<List<List<String>>> queryLines) {
        this.queryLines = queryLines;
    }

    @Override
    public int hashCode(){
        int result = 7;
        if (initialQuery != null) result = 31 * result + initialQuery.hashCode();
        for (List<List<String>> line: queryLines){
            for (List<String> wordgram: line){
                for (String word: wordgram){
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
                for (String word: queryLines.get(lineIdx).get(wgIdx)){
                    if (!pm.queryLines.get(lineIdx).get(wgIdx).contains(word)) return false;
                }
            }
        }
        return true;
    }
}
