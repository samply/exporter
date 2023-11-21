package de.samply.utils;

import de.samply.db.model.Query;
import de.samply.exporter.ExporterConst;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class QueryContextUtils {

    public static Map<String, String> fetchKeyValuesFromContext(Query query) {
        return (query != null) ? fetchKeyValuesFromContext(query.getContext()) : new HashMap<>();
    }

    public static Map<String, String> fetchKeyValuesFromContext(String queryContext) {
        Map<String, String> result = new HashMap<>();
        if (queryContext != null) {
            String decodedQueryContext = Base64Utils.decode(queryContext);
            Arrays.stream(decodedQueryContext.split(ExporterConst.QUERY_CONTEXT_SEPARATOR)).forEach(keyValue -> {
                int index = keyValue.indexOf(ExporterConst.QUERY_CONTEXT_EQUAL);
                if (index > 0) {
                    String key = keyValue.substring(0, index).trim();
                    if (key.length() > 0) {
                        String value = keyValue.substring(index + ExporterConst.QUERY_CONTEXT_EQUAL.length());
                        int index2 = value.indexOf("\"");
                        if (index2 >= 0) {
                            int index3 = value.substring(index2 + 1).indexOf("\"");
                            if (index3 >= 0) {
                                value = value.substring(index2 + 1, index2 + index3 + 1);
                            }
                        }
                        result.put(key, value.trim());
                    }
                }
            });

        }
        return result;
    }


}
