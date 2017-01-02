OpenService-BCB: Installation guide
===================================

## Requirements ##

- Runtime:
  - Linux/UNIX box
  - JRE 1.8+
  - [Apache Cassandra](http://cassandra.apache.org): application's main storage
  - [Redis](https://redis.io): (optional) application's cache storage
- Build from source:
  - GIT client
  - JDK 1.8+
  - [Activator 1.3+](https://www.playframework.com/download)


## Build from source ##

- Clone project at [https://github.com/VNGOpen/OpenService-BCB](https://github.com/VNGOpen/OpenService-BCB):
  - `[user@localhost ~]$ git clone https://github.com/VNGOpen/OpenService-BCB`
- Build with `activator`:
  - `[user@localhost ~]$ activator dist`


## Download binary ##

- Binary package can be downloaded at [https://github.com/VNGOpen/OpenService-BCB/releases](https://github.com/VNGOpen/OpenService-BCB/releases).


## Run the binary ##

- Unzip the binary package `service-bcb-x.y.z.zip` (where x.y.z is the version number)
- Create database schema & tables: see file `/dbschema/bcb.cql`
- Configure application's default port, memory limit, etc via file `conf/server.sh` or `conf/server-production.sh`
- Configure application's log via file `conf/logback.xml` or `conf/logback-production.xml`
- Configure application's database/cache connection settings via file `conf/spring/beans-cassandra-redis.xml`
- Start the server:
  - `[user@localhost service-bcb-x.y.z]$ sh conf/server-production.sh start`
- Stop the server:
  - `[user@localhost service-bcb-x.y.z]$ sh conf/server-production.sh stop`
