# Server setup for ZuMult

ZuMult is a Java EE application, so it wants a Java Web Application Server. All instances so far have been running under Apache Tomcat. 
ZuMult expects a number of additional components to be present on the server, and it needs some specific configurations for directories and for access control.
This document describes the setupt of the the ZuMult instance at the IDS Mannheim (https://zumult.ids-mannheim.de/) 
which has been running largely stable for a larger user-base for some years now.

## Basic setup

* Operating System: Cent OS (Virtual Machine) -- version?
* Tomcat Version: 9
* Java version: 11 (OpenJDK?)
* Do we need to specify Apache version? Or is that included in Tomcat? I never know...

As far as we know, using later Tomcat or Java versions would not break anything. 

## User database

The user database sits on a different machine. It is a single table in an Oracle RDB containing usernames and their passwords. There is no distinction between different roles. 

The user database is specified in `META-INF/context.xml`:

```
<Resource auth="Container" driverClassName="oracle.jdbc.OracleDriver" factory="org.apache.commons.dbcp.BasicDataSourceFactory" 
    maxActive="100" maxIdle="30" maxWait="10000" name="jdbc/TestDB" password="-" type="javax.sql.DataSource" url="-" username="-"/>
<Realm className="org.apache.catalina.realm.DataSourceRealm" dataSourceName="jdbc/TestDB" debug="99" localDataSource="true" 
    roleNameCol="CO_DS_READ" userCredCol="CO_PASS" userNameCol="CO_USER" userRoleTable="PRAGDB.T_USERS_EXT" userTable="PRAGDB.T_USERS_EXT"/>
```


  



