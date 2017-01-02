package bo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.github.ddth.dao.BaseBo;

/**
 * Consists of n-history records for a (name, key) pair.
 * 
 * <p>
 * A ranking history record consists of:
 * </p>
 * 
 * <ul>
 * <li>{@code name} (string) of the ranking</li>
 * <li>{@code key} (string): id/name of the target</li>
 * <li>List of {@code item}s in time order, each {@code item} is a record of:
 * <ul>
 * <li>{@code timestamp} (int)</li>
 * <li>{@code position} (int): rank position</li>
 * <li>{@code value} (number): rank value</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class RankingHistory extends BaseBo {

    /**
     * A ranking history record consists of:
     * 
     * <ul>
     * <li>{@code timestamp} (int)</li>
     * <li>{@code position} (int): rank position</li>
     * <li>{@code value} (number): rank value</li>
     * </ul>
     * 
     * @author ThanhNB
     * @since 0.1.0
     */
    public static class Item extends BaseItemBo {

        public final static Item[] EMPTY_ARRAY = new Item[0];

        public static Item newInstance(int timestamp, int position, double value) {
            Item bo = new Item();
            bo.setTimestamp(timestamp).setPosition(position).setValue(value);
            return bo;
        }

        public static Item newInstance(Rankings.Item item) {
            Item bo = new Item();
            bo.setPosition(item.getPosition());
            bo.setValue(item.getValue());
            return bo;
        }

        public Item clone() {
            return (Item) super.clone();
        }

        private final static String ATTR_TIMESTAMP = "t";
        private final static String ATTR_POSITION = "p";

        public int getTimestamp() {
            Integer value = getAttribute(ATTR_TIMESTAMP, Integer.class);
            return value != null ? value.intValue() : 0;
        }

        public Item setTimestamp(int timestamp) {
            return (Item) setAttribute(ATTR_TIMESTAMP, timestamp);
        }

        protected String encodeData() {
            return "" + getValueAsDouble() + DELIM + getPosition();
        }

        protected boolean decodeData(String data) {
            String[] tokens = data != null ? StringUtils.split(data, DELIM)
                    : ArrayUtils.EMPTY_STRING_ARRAY;
            setValue(tokens.length > 0 ? toDouble(tokens[0]) : 0.0);
            setPosition(tokens.length > 1 ? toInt(tokens[1]) : 0);
            return true;
        }

        public int getPosition() {
            Integer value = getAttribute(ATTR_POSITION, Integer.class);
            return value != null ? value.intValue() : 0;
        }

        public Item setPosition(int position) {
            setAttribute(ATTR_POSITION, position);
            return (Item) markDataChanged();
        }

    }

    public static RankingHistory newInstance(String name, String key, Item... items) {
        RankingHistory obj = new RankingHistory();
        obj.setName(name).setKey(key);
        obj.addItems(items);
        return obj;
    }

    private String name, key;
    private List<Item> itemList = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this);
        tsb.append("name", name).append("key", key).append("items", itemList);
        return tsb.toString();
    }

    public String getName() {
        return name;
    }

    public RankingHistory setName(String name) {
        this.name = name != null ? name.trim().toLowerCase() : "";
        return this;
    }

    public String getKey() {
        return key;
    }

    public RankingHistory setKey(String key) {
        this.key = key != null ? key.trim().toLowerCase() : "";
        return this;
    }

    public RankingHistory clearItems() {
        itemList.clear();
        return this;
    }

    public RankingHistory addItem(Item item) {
        if (item != null) {
            itemList.add(item);
        }
        return this;
    }

    public RankingHistory addItems(Item... items) {
        if (items != null) {
            for (Item item : items) {
                itemList.add(item);
            }
        }
        return this;
    }

    public Item[] getItems() {
        return itemList.toArray(Item.EMPTY_ARRAY);
    }

    public int getNumItems() {
        return itemList.size();
    }
}
