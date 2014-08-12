set APP_HOME=%CD%
set APP_JAR=%APP_HOME%\target\factbook-it.factbook.extraction-1.0-SNAPSHOT.jar
set START_CLASS=it.factbook.extraction.Start
set STOP_CLASS=%START_CLASS%
set START_METHOD=start
set STOP_METHOD=stop
set APP_LOGS_FOLDER=%APP_HOME%\logs
set APP_CONSOLE_LOG=%APP_LOGS_FOLDER%\console.log

if not exist "%APP_LOGS_FOLDER%" md "%APP_LOGS_FOLDER%"

prunsrv64.exe //IS//factbook-extraction --DisplayName "Factbook extraction"^
 --Description "Service of Factbook application to extract search results from search engines" --LogPath "%APP_LOGS_FOLDER%"^
 --Install "%APP_HOME%\prunsrv64.exe" --Jvm "%JAVA_HOME%\jre\bin\server\jvm.dll" --StartPath "%APP_HOME%" --StopPath "%APP_HOME%"^
 --Classpath "%APP_JAR%" --StartClass %START_CLASS% --StopClass %STOP_CLASS% --StartMethod %START_METHOD% --StopMethod %STOP_METHOD%^
 --StartMode jvm --StopMode jvm --StdOutput "%APP_CONSOLE_LOG%" --StdError "%APP_CONSOLE_LOG%"

 net start factbook-extraction