use extraction;

drop table if exists CrawlerLog;

create table CrawlerLog
(
    profileId             bigint,
    searchEngineId        int,
    profileVersion        bigint,
    urlHash               char(40),
    url                   varchar(2048) collate 'utf8_bin',
    found                 datetime,
    downloadStart         datetime,
    downloadTimeMsec      int,
    downloadSizeByte      int,
    primary key (profileId, searchEngineId, profileVersion),
    index Document_urlHash_idx (urlHash)
) ENGINE = MYISAM;