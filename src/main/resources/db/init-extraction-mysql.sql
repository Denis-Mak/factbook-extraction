use extraction;

drop table if exists CrawlerLog;

drop table if exists RequestLog;

drop table if exists RequestCashHit;

create table CrawlerLog
(
    profileId             bigint,
    searchEngineId        int,
    requestLogId          bigint,
    urlHash               char(40),
    golemId               int,
    url                   varchar(2048) collate 'utf8_bin',
    found                 datetime,
    downloadStart         datetime,
    downloadTimeMsec      int,
    downloadSizeByte      int,
    errorCode             int,
    errorMsg              varchar(2048),
    primary key (requestLogId, urlHash),
    index CrawlerLog_urlHash_idx (urlHash)
) ENGINE = MYISAM;


create table RequestLog
(
    requestLogId          bigint not null AUTO_INCREMENT,
    profileId             bigint,
    searchEngineId        int,
    profileVersion        bigint,
    golemId               int,
    query                 varchar(2048) collate 'utf8_bin',
    queryHash             char(40),
    requested             datetime,
    start                 int,
    resultsReturned       int,
    newLinks              int,
    primary key (requestLogId),
    index RequestLog_profileId_idx (profileId),
    index RequestLog_searchEngineId_queryHash_idx (searchEngineId, queryHash)
) ENGINE = MYISAM;

create table RequestCashHit
(
  queryHash             char(40),
  searchEngineId        int,
  hits                  int,
  primary key (queryHash, searchEngineId)
) ENGINE = MYISAM;