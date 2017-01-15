package bo.cassandra;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.ddth.cql.CqlUtils;
import com.github.ddth.cql.SessionManager;
import com.github.ddth.dao.BaseDao;

import bo.IBcbDao;
import bo.RankingHistory;
import bo.Rankings;
import play.Logger;
import utils.AppGlobals;

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

        return this;
    }

    private Session getSession() {
        return sessionManager.getSession(hostsAndPorts, username, password, keyspace);
    }

    private String CQL_GET_RANK = "SELECT n,t,p,d FROM {0} WHERE n=? AND t=?";
    private String CQL_GET_HISTORY = "SELECT n,k,t,d FROM {0} WHERE n=? AND k=? AND t>=? AND t<?";

    private String CQL_UPDATE_RANK = "UPDATE {0} SET d=? WHERE n=? AND t=? AND p=?";
    private String CQL_UPDATE_HISTORY = "UPDATE {0} SET d=? WHERE n=? AND k=? AND t=?";
    private String CQL_CLEANUP_HISTORY = "DELETE FROM {0} WHERE n=? AND k=? AND t=?";

    private void updateHistory(Session session, Rankings newRankings, Rankings existingRankings) {
        String name = newRankings.getName();
        int timestamp = newRankings.getTimestamp();
        Rankings.Item[] existingItems = existingRankings.getItems();
        Rankings.Item[] newItems = newRankings.getItems();
        Map<String, Object> newItemsMap = new HashMap<>();
        for (Rankings.Item item : newItems) {
            newItemsMap.put(item.getKey(), Boolean.TRUE);
        }

        /* Phase 1: cleanup old data */
        for (Rankings.Item item : existingItems) {
            if (newItemsMap.get(item.getKey()) == null) {
                CqlUtils.executeNonSelect(session, CQL_CLEANUP_HISTORY, ConsistencyLevel.LOCAL_ONE,
                        name, item.getKey(), timestamp);
            }
        }

        /* Phase 2: update with new data */
        for (Rankings.Item item : newItems) {
            RankingHistory.Item history = RankingHistory.Item.newInstance(item);
            CqlUtils.executeNonSelect(session, CQL_UPDATE_HISTORY, ConsistencyLevel.LOCAL_ONE,
                    history.getData(), name, item.getKey(), timestamp);
        }
    }

    private void updateHistory(Session session, BatchStatement batch, Rankings newRankings,
            Rankings existingRankings) {
        String name = newRankings.getName();
        int timestamp = newRankings.getTimestamp();
        Rankings.Item[] existingItems = existingRankings.getItems();
        Rankings.Item[] newItems = newRankings.getItems();
        Map<String, Object> newItemsMap = new HashMap<>();
        for (Rankings.Item item : newItems) {
            newItemsMap.put(item.getKey(), Boolean.TRUE);
        }

        /* Phase 1: cleanup old data */
        PreparedStatement stmCleanupHistory = CqlUtils.prepareStatement(session,
                CQL_CLEANUP_HISTORY);
        for (Rankings.Item item : existingItems) {
            if (newItemsMap.get(item.getKey()) == null) {
                batch.add(stmCleanupHistory.bind(name, item.getKey(), timestamp));
            }
        }

        /* Phase 2: update with new data */
        PreparedStatement stmUpdateHistory = CqlUtils.prepareStatement(session, CQL_UPDATE_HISTORY);
        for (Rankings.Item item : newItems) {
            RankingHistory.Item history = RankingHistory.Item.newInstance(item);
            batch.add(stmUpdateHistory.bind(history.getData(), name, item.getKey(), timestamp));
        }
    }

    private void updateRankings(Session session, Rankings rankings) {
        Rankings.Item[] items = rankings.getItems();
        Rankings.Item metaItem = Rankings.Item.newInstance(0, "_", items.length, "");
        String name = rankings.getName();
        int timestamp = rankings.getTimestamp();
        CqlUtils.executeNonSelect(session, CQL_UPDATE_RANK, ConsistencyLevel.LOCAL_ONE,
                metaItem.getData(), name, timestamp, 0);
        int pos = 0;
        for (Rankings.Item item : items) {
            CqlUtils.executeNonSelect(session, CQL_UPDATE_RANK, ConsistencyLevel.LOCAL_ONE,
                    item.getData(), name, timestamp, ++pos);
        }
    }

    private void updateRankings(Session session, BatchStatement batch, Rankings rankings) {
        Rankings.Item[] items = rankings.getItems();
        String name = rankings.getName();
        int timestamp = rankings.getTimestamp();
        PreparedStatement stmUpdateRank = CqlUtils.prepareStatement(session, CQL_UPDATE_RANK);
        Rankings.Item metaItem = Rankings.Item.newInstance(0, "_", items.length, "");
        batch.add(stmUpdateRank.bind(metaItem.getData(), name, timestamp, 0));
        int pos = 0;
        for (Rankings.Item item : items) {
            batch.add(stmUpdateRank.bind(item.getData(), name, timestamp, ++pos));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRankingsAndHistory(Rankings rankings) {
        Session session = getSession();
        String name = rankings.getName();
        int timestamp = rankings.getTimestamp();
        Rankings existingRankings = getRankings(session, name, timestamp, Integer.MAX_VALUE);

        Boolean batchMode = AppGlobals.appConfig.getBoolean("rankings_update_batch_mode");
        if (batchMode == null || !batchMode.booleanValue()) {
            if (Logger.isDebugEnabled()) {
                Logger.debug("Update rankings (non-batch): " + rankings);
            }
            updateHistory(session, rankings, existingRankings);
            updateRankings(session, rankings);
        } else {
            if (Logger.isDebugEnabled()) {
                Logger.debug("Update rankings (batch): " + rankings);
            }
            BatchStatement batch = new BatchStatement();
            batch.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
            updateHistory(session, batch, rankings, existingRankings);
            updateRankings(session, batch, rankings);
            session.execute(batch);
        }
    }

    /**
     * @since 0.1.1
     */
    private Rankings getRankings(Session session, String name, int timestamp, int limitNumRows) {
        Rankings result = Rankings.newInstance(name, timestamp, Rankings.Item.EMPTY_ARRAY);

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
    public Rankings getRankings(String name, int timestamp, int limitNumRows) {
        return getRankings(getSession(), name, timestamp, limitNumRows);
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
