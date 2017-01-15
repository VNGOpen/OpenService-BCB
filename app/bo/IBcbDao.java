package bo;

/**
 * @author ThanhNB
 * @since 0.1.0
 */
public interface IBcbDao {
    // /**
    // * Updates rankings data.
    // *
    // * @param rankings
    // */
    // public void updateRankings(Rankings rankings);
    //
    // /**
    // * Updates ranking history data.
    // *
    // * @param rankings
    // */
    // public void updateHistory(Rankings rankings);

    /**
     * Updates rankings and history data.
     * 
     * @param rankings
     */
    public void updateRankingsAndHistory(Rankings rankings);

    /**
     * Gets rankings data for a <code>{name:timestamp}</code>.
     * 
     * @param name
     * @param timestamp
     * @param limitNumRows
     * @return
     */
    public Rankings getRankings(String name, int timestamp, int limitNumRows);

    /**
     * Gets ranking history for a <code>{name:key}</code> during a period
     * <code>[timestampStart, timestampEnd)</code>.
     * 
     * @param name
     * @param key
     * @param timestampStart
     * @param timestampEnd
     * @return
     */
    public RankingHistory getHistory(String name, String key, int timestampStart, int timestampEnd);
}
