package utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.IdGenerator;

import play.Logger;
import play.mvc.Http.Request;

public class AppUtils {

    public final static IdGenerator IDGEN = IdGenerator.getInstance(IdGenerator.getMacAddr());

    public static String getClientIp(Request request) {
        String[] headerFields = new String[] { "X-Forwarded-For", "X-Real-IP", "Real-IP" };
        String clientIPHeader = null;
        for (String field : headerFields) {
            clientIPHeader = request.getHeader(field);
            if (!StringUtils.isBlank(clientIPHeader)) {
                if (Logger.isDebugEnabled()) {
                    Logger.debug(
                            "Got client ip [" + clientIPHeader + "] from field [" + field + "]");
                }
                break;
            }
        }
        if (StringUtils.isBlank(clientIPHeader)) {
            clientIPHeader = request.remoteAddress();
            if (Logger.isDebugEnabled()) {
                Logger.debug("Got client ip [" + clientIPHeader + "] from field [Remote-Address]");
            }
        }
        return clientIPHeader;
    }

    public static Collection<String> parseTags(Object tagsData) {
        Set<String> tags = new HashSet<>();
        if (tagsData instanceof List) {
            tagsData = ((List<?>) tagsData).toArray();
        }
        if (tagsData instanceof String[]) {
            for (String tag : (String[]) tagsData) {
                tags.add(tag);
            }
        } else if (tagsData instanceof Object[]) {
            for (Object tag : (Object[]) tagsData) {
                tags.add(tag.toString());
            }
        } else if (tagsData instanceof String) {
            String[] tokens = ((String) tagsData).split("[,;\\s]+");
            for (String tag : tokens) {
                tags.add(tag);
            }
        }
        return tags;
    }

    public static Map<String, String> parseRequestHeaders(Request request) {
        return parseRequestHeaders(request, ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public static Map<String, String> parseRequestHeaders(Request request, String... filters) {
        boolean hasFilters = false;
        if (filters != null && filters.length > 1) {
            Arrays.sort(filters);
            hasFilters = true;
        }
        Map<String, String> result = new HashMap<String, String>();
        Map<String, String[]> headers = request.headers();
        if (headers != null) {
            for (Entry<String, String[]> entry : headers.entrySet()) {
                String key = entry.getKey();
                if (!hasFilters || Arrays.binarySearch(filters, key) >= 0) {
                    String[] values = entry.getValue();
                    result.put(key, values != null & values.length > 0 ? values[0] : "");
                }
            }
        }
        return result;
    }

    public static String randomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    /**
     * Checks if API authentication is disabled (via configuration file).
     * 
     * @return
     */
    public static boolean isApiAuthDisabled() {
        Boolean value = AppGlobals.appConfig.getBoolean("auth.disabled");
        return value != null ? value.booleanValue() : false;
    }
}
