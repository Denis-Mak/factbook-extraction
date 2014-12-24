package it.factbook.extraction;

import it.factbook.dictionary.Golem;
import it.factbook.extraction.client.Request;
import it.factbook.extraction.client.SearchEngine;
import it.factbook.extraction.util.WebHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
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
        List<Link> decodedLinks = linksFound.stream()
                .map(l -> new Link(WebHelper.getDecodedURL(l.getUrl()), l.getTitle(), l.getSnippet(), l.getGolem()))
                .collect(Collectors.toList());
        String select = "SELECT urlHash FROM CrawlerLog WHERE urlHash IN (";
        Object[] params = new Object[linksFound.size()];
        int[] paramTypes = new int[linksFound.size()];
        for(int i = 0; i < linksFound.size(); i++){
            select += "?,";
            params[i] = decodedLinks.get(i).getUrlHash();
            paramTypes[i] = Types.CHAR;
        }
        select = select.substring(0,select.length()-1);
        select += ")";
        List<String> alreadyCrawledUrls = jdbcTemplate.query(select, params, paramTypes, (rs, rowNum) -> rs.getString("urlHash"));
        //remove links, that we have already had in our index
        List<Link> linksToCrawl = new ArrayList<>(linksFound.size());
        for (int i = 0; i < decodedLinks.size(); i++){
            if (!alreadyCrawledUrls.contains(decodedLinks.get(i).getUrlHash())){
                linksToCrawl.add(linksFound.get(i));
            }
        }
        return linksToCrawl;
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
        Timestamp foundTimestamp = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i)
                    throws SQLException {
                String docUrl = WebHelper.getDecodedURL(links.get(i).getUrl());
                ps.setLong      (1, profileId);
                ps.setInt       (2, searchEngine.getId());
                ps.setLong      (3, requestLogId);
                ps.setString    (4, DigestUtils.sha1Hex(docUrl));
                ps.setString    (5, docUrl);
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
            ps.setString    (7, DigestUtils.sha1Hex(WebHelper.getDecodedURL(url)));
        });
    }

    /**
     *
     *
     * @param profileId
     * @param searchEngine
     * @param profileVersion
     * @param request
     * @return
     */
    public long logSearchRequest(long profileId, SearchEngine searchEngine, long profileVersion, Request request){
        String INSERT = "INSERT INTO RequestLog (profileId, searchEngineId, profileVersion, golemId, query, queryHash, requested, start) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, profileId);
                    ps.setInt(2, searchEngine.getId());
                    ps.setLong(3, profileVersion);
                    ps.setInt(4, request.golem.getId());
                    ps.setString(5, request.query);
                    ps.setString(6, DigestUtils.sha1Hex(request.query));
                    ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                    ps.setInt(8, request.start);
                    return ps;
                }, keyHolder);
        return (long)keyHolder.getKey();
    }

    /**
     *
     * @param query
     * @param searchEngine
     * @return
     */
    public List<Request> getRequests(String query, SearchEngine searchEngine){
        String SELECT = "SELECT requestLogId, query, golemId, requested, start FROM RequestLog WHERE searchEngineId = ? AND queryHash = ?";
        return jdbcTemplate.query(SELECT,
                new Object[]{searchEngine.getId(), DigestUtils.sha1Hex(query)},
                new int[] {Types.INTEGER, Types.CHAR},
                new RequestRowMapper());
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

    /**
     *
     * @param query
     * @param searchEngine
     */
    public void incrementCashHits(String query, SearchEngine searchEngine) {
        String queryHash = DigestUtils.sha1Hex(query);
        int searchEngineId = searchEngine.getId();
        try {
            int hits = jdbcTemplate.queryForObject("SELECT hits FROM RequestCashHit WHERE queryHash = ? " +
                            "AND searchEngineId = ?",
                    new Object[]{queryHash, searchEngineId},
                    new int[] {Types.CHAR, Types.INTEGER},
                    Integer.class);
            jdbcTemplate.update("UPDATE RequestCashHit SET hits = ? WHERE searchEngineId = ? AND queryHash = ?",
                    new Object[]{hits + 1, searchEngineId, queryHash},
                    new int[]{Types.INTEGER, Types.INTEGER, Types.CHAR});
        } catch (DataAccessException e){
            jdbcTemplate.update("INSERT RequestCashHit(hits, searchEngineId, queryHash) VALUES(?,?,?)",
                    new Object[]{1, searchEngineId, queryHash},
                    new int[]{Types.INTEGER, Types.INTEGER, Types.CHAR});
        }
    }

    private static class RequestRowMapper implements ParameterizedRowMapper<Request> {
        @Override
        public Request mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Request(Golem.valueOf(rs.getInt("golemId")), rs.getString("query"), rs.getInt("start"), new DateTime(rs.getTimestamp("requested")));
        }
    }
}
