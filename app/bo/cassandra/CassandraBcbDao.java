package bo.cassandra;

import java.text.MessageFormat;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.ddth.cql.CqlUtils;
import com.github.ddth.cql.SessionManager;
import com.github.ddth.dao.BaseDao;

import bo.IBcbDao;
import bo.RankingHistory;
import bo.Rankings;

public class CassandraBcbDao extends BaseDao implements IBcbDao {

    private SessionManager sessionManager;
    private String hostsAndPorts = "localhost";
    private String username, password, keyspace = "stats_bcb";

    private String tableNameRank = "stats_bcb_rank";
    private String tableNameHistory = "stats_bcb_history";

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public CassandraBcbDao setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        return this;
    }

    public String getHostsAndPorts() {
        return hostsAndPorts;
    }

    public CassandraBcbDao setHostsAndPorts(String hostsAndPorts) {
        this.hostsAndPorts = hostsAndPorts;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public CassandraBcbDao setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public CassandraBcbDao setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public CassandraBcbDao setKeyspace(String keyspace) {
        this.keyspace = keyspace;
        return this;
    }

    public String getTableNameRank() {
        return tableNameRank;
    }

    public CassandraBcbDao setTableNameRank(String tableNameRank) {
        this.tableNameRank = tableNameRank;
        return this;
    }

    public String getTableNameHistory() {
        return tableNameHistory;
    }

    public CassandraBcbDao setTableNameHistory(String tableNameHistory) {
        this.tableNameHistory = tableNameHistory;
        return this;
    }

    public CassandraBcbDao init() {
        super.init();

        CQL_GET_RANK = MessageFormat.format(CQL_GET_RANK, tableNameRank);
        CQL_GET_HISTORY = MessageFormat.format(CQL_GET_HISTORY, tableNameHistory);

        CQL_UPDATE_RANK = MessageFormat.format(CQL_UPDATE_RANK, tableNameRank);
        CQL_UPDATE_HISTORY = MessageFormat.format(CQL_UPDATE_HISTORY, tableNameHistory);
        CQL_CLEANUP_HISTORY = MessageFormat.format(CQL_CLEANUP_HISTORY, tableNameHistory);
        CQL_SELECT_HISTORY_FOR_CLEANUP = MessageFormat.format(CQL_SELECT_HISTORY_FOR_CLEANUP,
                tableNameHistory);

        return this;
    }

    private Session getSession() {
        return sessionManager.getSession(hostsAndPorts, username, password, keyspace);
    }

    private String CQL_GET_RANK = "SELECT n,t,p,d FROM {0} WHERE n=? AND t=?";
    private String CQL_GET_HISTORY = "SELECT n,k,t,d FROM {0} WHERE n=? AND k=? AND t>=? AND t<?";

    private String CQL_UPDATE_RANK = "INSERT INTO {0} (n,t,p,d) VALUES (?,?,?,?)";
    private String CQL_UPDATE_HISTORY = "INSERT INTO {0} (n,k,t,d) VALUES (?,?,?,?)";
    private String CQL_CLEANUP_HISTORY = "DELETE FROM {0} WHERE n=? AND k=? AND t=?";
    private String CQL_SELECT_HISTORY_FOR_CLEANUP = "SELECT n,k,t,d FROM {0} WHERE n=? AND t=? ALLOW FILTERING";

    private void updateHistory(Session session, Rankings rankings) {
        String name = rankings.getName();
        int timestamp = rankings.getTimestamp();

        /* Phase 1: cleanup old data */
        ResultSet rs = CqlUtils.execute(session, CQL_SELECT_HISTORY_FOR_CLEANUP,
                ConsistencyLevel.LOCAL_ONE, name, timestamp);
        for (Row row : rs) {
            if (rs.getAvailableWithoutFetching() < 100 && !rs.isFullyFetched()) {
                rs.fetchMoreResults();
            }
            String key = row.getString("k");
            CqlUtils.executeNonSelect(session, CQL_CLEANUP_HISTORY, ConsistencyLevel.LOCAL_ONE,
                    name, key, timestamp);
        }

        /* Phase 2: update with new data */
        Rankings.Item[] items = rankings.getItems();
        for (Rankings.Item item : items) {
            RankingHistory.Item history = RankingHistory.Item.newInstance(item);
            CqlUtils.executeNonSelect(session, CQL_UPDATE_HISTORY, ConsistencyLevel.LOCAL_ONE, name,
                    item.getKey(), timestamp, history.getData());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateHistory(Rankings rankings) {
        updateHistory(getSession(), rankings);

    }

    private void updateRankings(Session session, Rankings rankings) {
        Rankings.Item[] items = rankings.getItems();
        Rankings.Item metaItem = Rankings.Item.newInstance(0, "_", items.length, "");
        String name = rankings.getName();
        int timestamp = rankings.getTimestamp();
        CqlUtils.executeNonSelect(session, CQL_UPDATE_RANK, ConsistencyLevel.LOCAL_ONE, name,
                timestamp, 0, metaItem.getData());
        int pos = 0;
        for (Rankings.Item item : items) {
            CqlUtils.executeNonSelect(session, CQL_UPDATE_RANK, ConsistencyLevel.LOCAL_ONE, name,
                    timestamp, ++pos, item.getData());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRankings(Rankings rankings) {
        updateRankings(getSession(), rankings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRankingsAndHistory(Rankings rankings) {
        Session session = getSession();
        updateRankings(session, rankings);
        updateHistory(session, rankings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rankings getRankings(String name, int timestamp, int limitNumRows) {
        Rankings result = Rankings.newInstance(name, timestamp, Rankings.Item.EMPTY_ARRAY);

        Session session = getSession();
        ResultSet rs = CqlUtils.execute(session, CQL_GET_RANK, ConsistencyLevel.LOCAL_ONE, name,
                timestamp);

        Rankings.Item metaItem = null;
        int numItems = 0;
        for (Row row : rs) {
            if (rs.getAvailableWithoutFetching() < 100 && !rs.isFullyFetched()) {
                rs.fetchMoreResults();
            }
            int pos = row.getInt("p");
            String data = row.getString("d");
            if (metaItem == null) {
                metaItem = new Rankings.Item();
                metaItem.setPosition(pos).setData(data);
                numItems = metaItem.getValueAsInt();
            } else {
                Rankings.Item item = new Rankings.Item();
                item.setPosition(pos).setData(data);
                result.addItem(item);
                if (result.getNumItems() >= numItems || result.getNumItems() >= limitNumRows) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankingHistory getHistory(String name, String key, int timestampStart,
            int timestampEnd) {
        RankingHistory result = RankingHistory.newInstance(name, key,
                RankingHistory.Item.EMPTY_ARRAY);

        Session session = getSession();
        ResultSet rs = CqlUtils.execute(session, CQL_GET_HISTORY, ConsistencyLevel.LOCAL_ONE, name,
                key, timestampStart, timestampEnd);

        for (Row row : rs) {
            if (rs.getAvailableWithoutFetching() < 100 && !rs.isFullyFetched()) {
                rs.fetchMoreResults();
            }
            int timestamp = row.getInt("t");
            String data = row.getString("d");
            RankingHistory.Item item = new RankingHistory.Item();
            item.setTimestamp(timestamp).setData(data);
            result.addItem(item);
        }

        return result;
    }
}
