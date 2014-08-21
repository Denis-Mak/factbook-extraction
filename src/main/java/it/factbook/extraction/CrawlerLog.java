package it.factbook.extraction;

import it.factbook.extraction.client.Query;
import it.factbook.extraction.client.SearchEngine;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
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
     * @param requestLogId - ID of request log record. Must identify request that returns these results
     * @param links - list of links to save
     */
    public void logFoundLinks(long profileId, SearchEngine searchEngine, long requestLogId, List<Link> links){
        String INSERT = "INSERT INTO CrawlerLog (profileId, searchEngineId, requestLogId, urlHash, url, golemId, found) " +
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
                ps.setLong      (3, requestLogId);
                ps.setString    (4, links.get(i).getUrlHash());
                ps.setString    (5, links.get(i).getUrl());
                ps.setInt       (6, links.get(i).getGolem().getId());
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
     * @param requestLogId
     * @param url
     * @param articleBody
     * @param downloadStart
     */
    public void logDownloadedArticles(long requestLogId, String url, String articleBody,
                                      long downloadStart, String errorMsg){
        String UPDATE = "UPDATE CrawlerLog SET downloadStart=?, downloadTimeMsec=?, downloadSizeByte=?, errorCode=?, errorMsg=? " +
                "WHERE requestLogId=? AND urlHash=?";
        int downloadTime = new Long(System.currentTimeMillis() - downloadStart).intValue();
        int errCode = 200;
        if (errorMsg.contains("Server returned HTTP response code")){
            errCode = Integer.parseInt(errorMsg.substring(errorMsg.indexOf("code:") + 6, errorMsg.indexOf(" for URL:")));
        }
        final int errCodeFinal = errCode;
        jdbcTemplate.update(UPDATE
                , ps -> {
            ps.setTimestamp (1, new Timestamp(downloadStart));
            ps.setInt       (2, downloadTime);
            ps.setInt       (3, articleBody.length());
            ps.setInt       (4, errCodeFinal);
            ps.setString    (5, errorMsg);
            ps.setLong      (6, requestLogId);
            ps.setString    (7, DigestUtils.sha1Hex(url));
        });
    }

    /**
     *
     *
     * @param profileId
     * @param searchEngine
     * @param profileVersion
     * @param query
     * @return
     */
    public long logSearchRequest(long profileId, SearchEngine searchEngine, long profileVersion, Query query){
        String INSERT = "INSERT INTO RequestLog (profileId, searchEngineId, profileVersion, golemId, query, queryHash, requested) " +
                "VALUES (?,?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, profileId);
                    ps.setInt(2, searchEngine.getId());
                    ps.setLong(3, profileVersion);
                    ps.setInt(4, query.golem.getId());
                    ps.setString(5, query.query);
                    ps.setString(6, DigestUtils.sha1Hex(query.query));
                    ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                    return ps;
                }, keyHolder);
        return (long)keyHolder.getKey();
    }

    /**
     *
     * @param requestLogId
     * @param resultsReturned
     */
    public void logReturnedResults(long requestLogId, int resultsReturned, int newLinks){
        String UPDATE = "UPDATE RequestLog SET resultsReturned=?, newLinks=? WHERE requestLogId=?";
        jdbcTemplate.update(UPDATE, new Object[]{resultsReturned, newLinks, requestLogId},
                new int[]{Types.INTEGER, Types.INTEGER, Types.BIGINT});
    }
}
