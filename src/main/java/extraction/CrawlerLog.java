package extraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import se.client.SearchEngine;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Repository
public class CrawlerLog {
    @Autowired
    private static DataSource dataSource;

    private static JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    public List<Link> getLinksToCrawl(List<Link> linksFound){
        String select = "SELECT urlHash FROM CrawlerLog WHERE urlHash IN (";
        Object[] params = new Object[linksFound.size()];
        int[] paramTypes = new int[linksFound.size()];
        for(int i = 0; i < linksFound.size(); i++){
            select += "?,";
            params[i] = linksFound.get(i).getUrlHash();
            paramTypes[i] = Types.CHAR;
        }
        select = select.substring(0,select.length()-1);
        List<String> alreadyCrawledUrls = jdbcTemplate.query(select, params, paramTypes, (rs, rowNum) -> rs.getString("urlHash"));
        //remove links, that we have already had in our index
        return linksFound.stream()
                .filter(l -> !(alreadyCrawledUrls.contains(l.getUrlHash())))
                .collect(Collectors.toList());
    }



    public void logFoundLinks(long profileId, SearchEngine searchEngine, long profileVersion, List<Link> links){
        String INSERT = "INSERT INTO CrawlerLog (profileId, searchEngineId, profileVersion, urlHash, url, found) " +
                "VALUES (?,?,?,?,?,?)";
        // Use timestamp as part of primary key, because impossible get surrogate keys after batch update,
        // but we need to update log records later to keep crawled timestamp
        Timestamp foundTimestamp = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                ps.setLong(1, profileId);
                ps.setInt(2, searchEngine.getId());
                ps.setLong(3, profileVersion);
                ps.setString(3, links.get(i).getUrlHash());
                ps.setString(4, links.get(i).getUrl());
                ps.setTimestamp(5, foundTimestamp);
            }

            @Override
            public int getBatchSize() {
                return links.size();
            }
        });
    }

    public void logDownloadedArticles(long profileId, SearchEngine searchEngine, long profileVersion,
                                      String articleBody, long downloadStart){
        String UPDATE = "UPDATE CrawlerLog SET downloadStart=?, downloadTimeMsec=?, downloadSizeByte=? " +
                "WHERE profileId=? AND searchEngineId=? AND profileVersion=?";
        int downloadTime = new Long(System.currentTimeMillis() - downloadStart).intValue();
        jdbcTemplate.update(UPDATE
                , ps -> {
            ps.setTimestamp(1, new Timestamp(downloadStart));
            ps.setInt(2, downloadTime);
            ps.setInt(3, articleBody.length());
            ps.setLong(4, profileId);
            ps.setInt(5, searchEngine.getId());
            ps.setLong(6, profileVersion);
        });
    }
}
