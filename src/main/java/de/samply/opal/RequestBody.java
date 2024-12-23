package de.samply.opal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestBody{
    private final Map<String, Object> requestMap;

    public RequestBody(String characterSet, int firstRow, String quote, String separator,
                                String defaultValueType, List<Map<String, String>> tables) {

        Map<String, Object> params = new HashMap<>();
        params.put("characterSet", characterSet);
        params.put("firstRow", firstRow);
        params.put("quote", quote);
        params.put("separator", separator);
        params.put("defaultValueType", defaultValueType);
        params.put("tables", tables);

        requestMap = Map.of("Magma.CsvDatasourceFactoryDto.params", params);
    }

    public Map<String, Object> getRequestMap() {
        return requestMap;
    }

    public static Map<String, String> createTable(String name, String data, String entityType, String refTable) {
        return Map.of(
                "name", name,
                "data", data,
                "entityType", entityType,
                "refTable", refTable
        );
    }
}