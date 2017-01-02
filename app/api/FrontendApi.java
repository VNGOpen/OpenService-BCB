package api;

import java.util.Collection;
import java.util.Map;

import bo.IBcbDao;
import bo.RankingHistory;
import bo.Rankings;

public class FrontendApi {

    private IBcbDao bcbDao;

    protected IBcbDao getBcbDao() {
        return bcbDao;
    }

    public FrontendApi setBcbDao(IBcbDao bcbDao) {
        this.bcbDao = bcbDao;
        return this;
    }

    public FrontendApi init() {
        return this;
    }

    public void destroy() {
        // EMPTY
    }

    /*----------------------------------------------------------------------*/

    private Rankings updateRankings(Rankings rankings) {
        bcbDao.updateRankingsAndHistory(rankings);
        return rankings;
    }

    public Rankings updateRankings(String name, int timestamp,
            Collection<Map<String, Object>> itemsData) {
        return updateRankings(Rankings.newInstance(name, timestamp, itemsData));
    }

    private Rankings getRankings(String name, int timestamp, int limitNumRecords) {
        return bcbDao.getRankings(name, timestamp, limitNumRecords);
    }

    public Rankings getRankings(String name, int timestamp) {
        return getRankings(name, timestamp, Integer.MAX_VALUE);
    }

    public RankingHistory getHistory(String name, String key, int timestampStart,
            int timestampEnd) {
        return bcbDao.getHistory(name, key, timestampStart, timestampEnd);
    }
}
