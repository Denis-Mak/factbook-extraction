package it.factbook.extraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import it.factbook.extraction.client.SearchEngine;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Repository
public class CrawlerLog {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("extractionDataSource")
    private void setJdbcTemplate(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Method returns links that haven't downloaded earlier
     * @param linksFound - list of links you want to check
     * @return list of links haven't ever downloaded
     */
    public List<Link> getLinksToCrawl(List<Link> linksFound){
        if (linksFound == null || linksFound.size() == 0) return new ArrayList<>();
        String select = "SELECT urlHash FROM CrawlerLog WHERE urlHash IN (";
        Object[] params = new Object[linksFound.size()];
        int[] paramTypes = new int[linksFound.size()];
        for(int i = 0; i < linksFound.size(); i++){
            select += "?,";
            params[i] = linksFound.get(i).getUrlHash();
            paramTypes[i] = Types.CHAR;
        }
        select = select.substring(0,select.length()-1);
        select += ")";
        List<String> alreadyCrawledUrls = jdbcTemplate.query(select, params, paramTypes, (rs, rowNum) -> rs.getString("urlHash"));
        //remove links, that we have already had in our index
        return linksFound.stream()
                .filter(l -> !(alreadyCrawledUrls.contains(l.getUrlHash())))
                .collect(Collectors.toList());
    }


    /**
     * Write to log all links passed to the method
     * @param profileId - ID of profile these links found for
     * @param searchEngine - enum value of Search Engine client, that found these links. One for all links.
     * @param profileVersion - unique identifier of the version of profile, needed because profile changes during time.
     *                       Normally it is a timestamp in millis.
     * @param links - list of links to save
     */
    public void logFoundLinks(long profileId, SearchEngine searchEngine, long profileVersion, List<Link> links){
        String INSERT = "INSERT INTO CrawlerLog (profileId, searchEngineId, profileVersion, urlHash, url, golemId, found) " +
                "VALUES (?,?,?,?,?,?,?)";
        // Use timestamp as part of primary key, because impossible get surrogate keys after batch update,
        // but we need to update log records later to keep crawled timestamp
        Timestamp foundTimestamp = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                ps.setLong      (1, profileId);
                ps.setInt       (2, searchEngine.getId());
                ps.setLong      (3, profileVersion);
                ps.setString    (4, links.get(i).getUrlHash());
                ps.setString    (5, links.get(i).getUrl());
                ps.setInt       (6, links.get(i).getGolemId());
                ps.setTimestamp (7, foundTimestamp);
            }

            @Override
            public int getBatchSize() {
                return links.size();
            }
        });
    }

    /**
     * Add to link records info about downloaded document
     * @param profileId
     * @param searchEngine
     * @param profileVersion
     * @param link
     * @param articleBody
     * @param downloadStart
     */
    public void logDownloadedArticles(long profileId, SearchEngine searchEngine, long profileVersion, Link link,
                                      String articleBody, long downloadStart){
        String UPDATE = "UPDATE CrawlerLog SET downloadStart=?, downloadTimeMsec=?, downloadSizeByte=? " +
                "WHERE profileId=? AND searchEngineId=? AND profileVersion=? AND urlHash=?";
        int downloadTime = new Long(System.currentTimeMillis() - downloadStart).intValue();
        jdbcTemplate.update(UPDATE
                , ps -> {
            ps.setTimestamp (1, new Timestamp(downloadStart));
            ps.setInt       (2, downloadTime);
            ps.setInt       (3, articleBody.length());
            ps.setLong      (4, profileId);
            ps.setInt       (5, searchEngine.getId());
            ps.setLong      (6, profileVersion);
            ps.setString    (7, link.getUrlHash());
        });
    }
}
