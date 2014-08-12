use it.factbook.extraction;

drop table if exists CrawlerLog;

create table CrawlerLog
(
    profileId             bigint,
    searchEngineId        int,
    profileVersion        bigint,
    urlHash               char(40),
    golemId               int,
    url                   varchar(2048) collate 'utf8_bin',
    found                 datetime,
    downloadStart         datetime,
    downloadTimeMsec      int,
    downloadSizeByte      int,
    primary key (profileId, searchEngineId, profileVersion, urlHash),
    index Document_urlHash_idx (urlHash)
) ENGINE = MYISAM;