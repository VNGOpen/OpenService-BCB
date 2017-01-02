package utils;

import java.nio.charset.Charset;

/**
 * Commonly used constants.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class AppConstants {

    public final static Charset UTF8 = Charset.forName("UTF-8");

    public final static int LOGIN_CHANNEL_DEFAULT = 0;
    public final static int LOGIN_CHANNEL_GPLUS = 1;
    public final static int LOGIN_CHANNEL_FB = 2;

    public final static String FLASH_MSG_PREFIX_ERROR = "_E_:";
    public final static String FLASH_MSG_PREFIX_WARNING = "_W_:";

    public final static String DF_FULL = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static String DF_YYYYMMDD = "yyyy-MM-dd";
    public final static String DF_HHMMSS = "HH:mm:ss";
    public final static String DF_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";

    public final static int RESPONSE_OK = 200;
    public final static int RESPONSE_NOT_FOUND = 404;
    public final static int RESPONSE_ACCESS_DENIED = 403;
    public final static int RESPONSE_CLIENT_ERROR = 400;
    public final static int RESPONSE_SERVER_ERROR = 500;
}
