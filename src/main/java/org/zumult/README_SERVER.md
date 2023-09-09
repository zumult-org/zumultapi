# Server setup for ZuMult

ZuMult is a Java EE application, so it wants a Java Web Application Server. All instances so far have been running under Apache Tomcat. 
ZuMult expects a number of additional components to be present on the server, and it needs some specific configurations for directories and for access control.
This document describes the setup of the the ZuMult instance at the IDS Mannheim (https://zumult.ids-mannheim.de/) 
which has been running largely stable for a larger user-base for some years now.

## Basic setup

* Operating System: Cent OS (Virtual Machine) -- __version__?
* Tomcat Version: 9
* Java version: 11 (__OpenJDK?__)
* __Do we need to specify Apache version? Or is that included in Tomcat? I never know...__

As far as we know, using newer Tomcat or Java versions would not break anything. 

## User database

The user database sits on a different machine. It is a single table in an Oracle RDB containing usernames and their passwords. There is no distinction between different roles. 

The user database is specified in `META-INF/context.xml`:

```
<Resource auth="Container" driverClassName="oracle.jdbc.OracleDriver" factory="org.apache.commons.dbcp.BasicDataSourceFactory" 
    maxActive="100" maxIdle="30" maxWait="10000" name="jdbc/TestDB" password="-" type="javax.sql.DataSource" url="-" username="-"/>
<Realm className="org.apache.catalina.realm.DataSourceRealm" dataSourceName="jdbc/TestDB" debug="99" localDataSource="true" 
    roleNameCol="CO_DS_READ" userCredCol="CO_PASS" userNameCol="CO_USER" userRoleTable="PRAGDB.T_USERS_EXT" userTable="PRAGDB.T_USERS_EXT"/>
```

The same approach should work for any other RDB as long as the application has permission to access the respective table (via url/username/password) 
and there is a JDBC driver for it.


## Corpus data

Audio, video, transcripts and metadata are not packaged with the application. 
The audio and video files sit in different folders on an external drive which is mounted on the application server.
The metadata and transcript files are under a specific folder on the same machine as the application.
Required storage space is roughly 1GB per hour of audio, 5GB per hour of video and 1.5MB per hour of transcript (including all annotations).
The directories containing audio/video files are made available by the web server (Apache?) for streaming via http, but direct download of these files is not permitted. 
(__What setting in what configuration is this?__) 

## Search index

For the query part of the application, a Lucene index (a number of binary files, organised in folders) is calculated and has to be accessible from the application. 
We found significant differences in query performance when this index was placed on the same machine (as opposed to accessed over a network drive).
The index takes up about 3MB of disk space per hour of transcribed (and fully annotated) data. 

## FFMpeg

For cutting (or otherwise manipulating) video files, the application calls an FFMpeg executable which has to be installed on the server with the necessary permissions.
Cutting audio is achieved with pure Java, so there is no need to install FFMpeg if the application does not deal with video data.

## Download directory

When the application delivers downloadable data, it puts them in a designated folder. The application needs writing permissions on that folder, 
and the contents of the folder have to be accessible via http. To avoid unnecessary disk space usage, we have a cron job running on the server which 
looks into that folder in regular intervals (__20 minutes?__) and deletes all files with a timestamp before the last run (i.e. files older than __20 minutes__).






  



