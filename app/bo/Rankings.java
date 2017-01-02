package bo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.dao.BaseBo;

/**
 * Consists of n-ranking records for a (name, timestamp) pair.
 * 
 * <p>
 * A ranking record consists of:
 * </p>
 * 
 * <ul>
 * <li>{@code name} (string) of the ranking</li>
 * <li>{@code timestamp} (int)</li>
 * <li>List of {@code item}s in order, each {@code item} is a record of:
 * <ul>
 * <li>{@code position} (int): rank position</li>
 * <li>{@code key} (string): id/name of the target</li>
 * <li>{@code value} (number): rank value</li>
 * <li>{@code info} (string): other information</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class Rankings extends BaseBo {

    /**
     * A ranking record consists of:
     * 
     * <ul>
     * <li>{@code position} (int): rank position</li>
     * <li>{@code key} (string): id/name of the target</li>
     * <li>{@code value} (number): rank value</li>
     * <li>{@code info} (string): other information</li>
     * </ul>
     * 
     * @author ThanhNB
     * @since 0.1.0
     */
    public static class Item extends BaseItemBo {

        public final static Item[] EMPTY_ARRAY = new Item[0];

        public static Item newInstance(int position, String key, double value, String info) {
            Item bo = new Item();
            bo.setPosition(position).setKey(key).setInfo(info).setValue(value);
            return bo;
        }

        public Item clone() {
            return (Item) super.clone();
        }

        private final static String ATTR_POSITION = "p";
        private final static String ATTR_KEY = "k";
        private final static String ATTR_INFO = "i";

        public int getPosition() {
            Integer value = getAttribute(ATTR_POSITION, Integer.class);
            return value != null ? value.intValue() : 0;
        }

        public Item setPosition(int position) {
            return (Item) setAttribute(ATTR_POSITION, position);
        }

        protected String encodeData() {
            return "" + getValueAsDouble() + DELIM + getKey() + DELIM + getInfo();
        }

        protected boolean decodeData(String data) {
            String[] tokens = data != null ? StringUtils.split(data, DELIM)
                    : ArrayUtils.EMPTY_STRING_ARRAY;
            setValue(tokens.length > 0 ? toDouble(tokens[0]) : 0.0);
            setKey(tokens.length > 1 ? tokens[1] : "");
            setInfo(tokens.length > 2 ? tokens[2] : "");

            return true;
        }

        public String getKey() {
            return getAttribute(ATTR_KEY, String.class);
        }

        public Item setKey(String key) {
            setAttribute(ATTR_KEY, key != null ? key.trim().toLowerCase() : "");
            return (Item) markDataChanged();
        }

        public String getInfo() {
            return getAttribute(ATTR_INFO, String.class);
        }

        public Item setInfo(String info) {
            setAttribute(ATTR_INFO, info != null ? info.trim() : "");
            return (Item) markDataChanged();
        }
    }

    public static Rankings newInstance(String name, int timestamp, Item... items) {
        Rankings obj = new Rankings();
        obj.setName(name).setTimestamp(timestamp);
        obj.addItems(items);
        return obj;
    }

    @SuppressWarnings("unchecked")
    public static Rankings newInstance(String name, int timestamp,
            Collection<Map<String, Object>> itemsData) {
        return newInstance(name, timestamp,
                itemsData != null ? itemsData.toArray(new Map[0]) : null);
    }

    public static Rankings newInstance(String name, int timestamp,
            Map<String, Object>[] itemsData) {
        Collection<Item> items = new ArrayList<>();
        if (itemsData != null) {
            int pos = 1;
            for (Map<String, Object> itemData : itemsData) {
                String key = DPathUtils.getValue(itemData, "id", String.class);
                if (key == null) {
                    key = DPathUtils.getValue(itemData, "key", String.class);
                    if (key == null) {
                        key = DPathUtils.getValue(itemData, "k", String.class);
                    }
                }
                Double value = DPathUtils.getValue(itemData, "value", Double.class);
                if (value == null) {
                    value = DPathUtils.getValue(itemData, "v", Double.class);
                }
                String info = DPathUtils.getValue(itemData, "info", String.class);
                if (info == null) {
                    info = DPathUtils.getValue(itemData, "i", String.class);
                }
                Item item = Item.newInstance(pos, key, value != null ? value.doubleValue() : 0.0,
                        info);
                if (items != null) {
                    pos++;
                    items.add(item);
                }
            }
        }
        return newInstance(name, timestamp, items.toArray(Item.EMPTY_ARRAY));
    }

    private String name;
    private int timestamp;
    private List<Item> itemList = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this);
        tsb.append("name", name).append("timestamp", timestamp).append("items", itemList);
        return tsb.toString();
    }

    public String getName() {
        return name;
    }

    public Rankings setName(String name) {
        this.name = name != null ? name.trim().toLowerCase() : "";
        return this;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public Rankings setTimestamp(int timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Rankings clearItems() {
        itemList.clear();
        return this;
    }

    public Rankings addItem(Item item) {
        if (item != null) {
            itemList.add(item);
        }
        return this;
    }

    public Rankings addItems(Item... items) {
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
