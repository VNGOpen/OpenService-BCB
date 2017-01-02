package bo;

import com.github.ddth.dao.BaseBo;

/**
 * @author ThanhNB
 * @since 0.1.0
 */
public abstract class BaseItemBo extends BaseBo {

    protected final static char DELIM = '\u0001';

    private final static String ATTR_VALUE = "v";
    private final static String ATTR_DATA = "d";

    private boolean dataChanged = false;

    protected boolean isDataChanged() {
        return dataChanged;
    }

    protected BaseItemBo markDataChanged() {
        dataChanged = true;
        return this;
    }

    protected BaseItemBo markDataUnchanged() {
        dataChanged = false;
        return this;
    }

    protected abstract String encodeData();

    protected abstract boolean decodeData(String data);

    protected double toDouble(String src) {
        try {
            return src != null ? Double.parseDouble(src) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    protected int toInt(String src) {
        try {
            return src != null ? Integer.parseInt(src) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    protected long toLong(String src) {
        try {
            return src != null ? Long.parseLong(src) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getData() {
        if (isDataChanged()) {
            setAttribute(ATTR_DATA, encodeData());
            markDataUnchanged();
        }
        String data = getAttribute(ATTR_DATA, String.class);
        return data;
    }

    public BaseItemBo setData(String data) {
        if (decodeData(data)) {
            setAttribute(ATTR_DATA, data);
            markDataUnchanged();
        }
        return (BaseItemBo) setAttribute(ATTR_DATA, data);
    }

    protected Number getValue() {
        Object value = getAttribute(ATTR_VALUE);
        return value instanceof Number ? (Number) value : null;
    }

    protected BaseItemBo setValue(Number value) {
        setAttribute(ATTR_VALUE, value);
        return markDataChanged();
    }

    public byte getValueAsByte() {
        Number value = getValue();
        return value != null ? value.byteValue() : 0;
    }

    public short getValueAsShort() {
        Number value = getValue();
        return value != null ? value.shortValue() : 0;
    }

    public int getValueAsInt() {
        Number value = getValue();
        return value != null ? value.intValue() : 0;
    }

    public long getValueAsLong() {
        Number value = getValue();
        return value != null ? value.longValue() : 0;
    }

    public float getValueAsFloat() {
        Number value = getValue();
        return value != null ? value.floatValue() : (float) 0.0;
    }

    public double getValueAsDouble() {
        Number value = getValue();
        return value != null ? value.doubleValue() : 0.0;
    }

    public BaseItemBo setValue(byte value) {
        return setValue(Byte.valueOf(value));
    }

    public BaseItemBo setValue(short value) {
        return setValue(Short.valueOf(value));
    }

    public BaseItemBo setValue(int value) {
        return setValue(Integer.valueOf(value));
    }

    public BaseItemBo setValue(long value) {
        return setValue(Long.valueOf(value));
    }

    public BaseItemBo setValue(float value) {
        return setValue(Float.valueOf(value));
    }

    public BaseItemBo setValue(double value) {
        return setValue(Double.valueOf(value));
    }
}
