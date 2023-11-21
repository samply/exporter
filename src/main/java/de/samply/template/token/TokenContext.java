package de.samply.template.token;

import de.samply.db.model.Query;
import de.samply.utils.EnvironmentUtils;
import de.samply.utils.QueryContextUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TokenContext {

    private final EnvironmentUtils environmentUtils;
    private final Map<String, String> keyValues = new HashMap<>();

    public TokenContext(EnvironmentUtils environmentUtils) {
        this.environmentUtils = environmentUtils;
    }

    public void addKeyValues(HttpServletRequest httpServletRequest) {
        if (httpServletRequest != null) {
            httpServletRequest.getParameterMap().keySet().forEach(key -> {
                String[] values = httpServletRequest.getParameterMap().get(key);
                if (values != null && values.length >= 1) {
                    keyValues.put(key, values[0]);
                }
            });
        }
    }

    public void addKeyValues(String key, String value) {
        if (key != null && value != null) {
            keyValues.put(key, value);
        }
    }

    public void addKeyValues(Map<String, String> keyValues) {
        this.keyValues.putAll(keyValues);
    }

    public void addKeyValues(Query query) {
        addKeyValues(QueryContextUtils.fetchKeyValuesFromContext(query));
    }

    public void addKeyValuesFromQueryContext(String queryContext) {
        addKeyValues(QueryContextUtils.fetchKeyValuesFromContext(queryContext));
    }

    public Map<String, String> getKeyValues() {
        return keyValues;
    }

    public Optional<String> getValue(String key) {
        String result = keyValues.get(key);
        if (result == null && environmentUtils != null) {
            result = environmentUtils.getEnvironmentVariable(key);
        }
        return Optional.ofNullable(result);
    }

    public EnvironmentUtils getEnvironmentUtils() {
        return environmentUtils;
    }
}
