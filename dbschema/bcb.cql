-- Cassandra schema for BCB

-- Keyspace
--CREATE KEYSPACE stats_bcb_dev WITH replication={'class':'SimpleStrategy','replication_factor':'1'} AND durable_writes=TRUE;
--CREATE KEYSPACE stats_bcb WITH replication={'class':'SimpleStrategy','replication_factor':'1'} AND durable_writes=TRUE;

-- User
--CREATE USER stats_bcb WITH PASSWORD 'stats_bcb' NOSUPERUSER;
--GRANT ALTER  ON KEYSPACE stats_bcb_dev TO stats_bcb;
--GRANT CREATE ON KEYSPACE stats_bcb_dev TO stats_bcb;
--GRANT DROP   ON KEYSPACE stats_bcb_dev TO stats_bcb;
--GRANT MODIFY ON KEYSPACE stats_bcb_dev TO stats_bcb;
--GRANT SELECT ON KEYSPACE stats_bcb_dev TO stats_bcb;

--GRANT ALTER  ON KEYSPACE stats_bcb TO stats_bcb;
--GRANT CREATE ON KEYSPACE stats_bcb TO stats_bcb;
--GRANT DROP   ON KEYSPACE stats_bcb TO stats_bcb;
--GRANT MODIFY ON KEYSPACE stats_bcb TO stats_bcb;
--GRANT SELECT ON KEYSPACE stats_bcb TO stats_bcb;


-- Table to store ranking
DROP TABLE IF EXISTS stats_bcb_rank;
CREATE TABLE stats_bcb_rank (
    n           varchar,    -- name of the ranking (e.g. "top power", "top level", etc)
    t           int,        -- timestamp
    p           int,        -- position (0,1,2...)
    d           text,       -- data
    PRIMARY KEY ((n,t), p)
) WITH COMPACT STORAGE;

-- Table to store ranking history
DROP TABLE IF EXISTS stats_bcb_history;
CREATE TABLE stats_bcb_history (
    n           varchar,    -- name of the ranking (e.g. "top power", "top level", etc)
    k           varchar,    -- id/target
    t           int,        -- timestamp
    d           text,       -- data
    PRIMARY KEY ((n,k), t)
) WITH CLUSTERING ORDER BY (t DESC);

DROP INDEX IF EXISTS stats_bcb_history_n_idx;
CREATE INDEX stats_bcb_history_n_idx ON stats_bcb_history(n);
DROP INDEX IF EXISTS stats_bcb_history_t_idx;
CREATE INDEX stats_bcb_history_t_idx ON stats_bcb_history(t);
