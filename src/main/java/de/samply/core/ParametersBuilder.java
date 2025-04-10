package de.samply.core;

import de.samply.converter.Format;
import de.samply.db.model.Query;
import de.samply.exporter.ExporterConst;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ParametersBuilder {

    private final Map<String, String> parameters;

    private static final Map<Class, Converter> classConverterMap = initializeClassConverterMap();


    public ParametersBuilder(@NotNull Map<String, String> parameters) {
        this.parameters = parameters;
    }

    private static Map<Class, Converter> initializeClassConverterMap() {
        Map<Class, Converter> classConverterMap = new HashMap<>();
        classConverterMap.put(String.class, x -> x);
        classConverterMap.put(Long.class, x -> Long.valueOf(x));
        classConverterMap.put(Format.class, x -> Format.valueOf(x));
        classConverterMap.put(LocalDate.class, x -> LocalDate.parse(x, DateTimeFormatter.ISO_DATE));
        return classConverterMap;
    }

    public ExporterParameters buildExporterParameters() {
        Long queryId = fetchValue(ExporterConst.QUERY_ID, Long.class);
        String query = fetchValue(ExporterConst.QUERY);
        String templateId = fetchValue(ExporterConst.TEMPLATE_ID);
        String requestTemplate = fetchValue(ExporterConst.REQUEST_TEMPLATE);
        String contentType = fetchValue(HttpHeaders.CONTENT_TYPE);
        Format queryFormat = fetchValue(ExporterConst.QUERY_FORMAT, Format.class);
        String queryLabel = fetchValue(ExporterConst.QUERY_LABEL);
        String queryDescription = fetchValue(ExporterConst.QUERY_DESCRIPTION);
        String queryContactId = fetchValue(ExporterConst.QUERY_CONTACT_ID);
        String queryContext = fetchValue(ExporterConst.QUERY_CONTEXT);
        String queryExecutionContactId = fetchValue(ExporterConst.QUERY_EXECUTION_CONTACT_ID);
        LocalDate queryExpirationDate = fetchValue(ExporterConst.QUERY_EXPIRATION_DATE, LocalDate.class);
        Format outputFormat = fetchValue(ExporterConst.OUTPUT_FORMAT, Format.class);

        return new ExporterParameters(queryId, query, templateId, requestTemplate, contentType, queryFormat, queryLabel,
                queryDescription, queryContactId, queryContext, queryExecutionContactId, queryExpirationDate, outputFormat);
    }

    public Query buildQuery(){
        Query query = new Query();
        query.setQuery(fetchValue(ExporterConst.QUERY));
        query.setFormat(fetchValue(ExporterConst.QUERY_FORMAT, Format.class));
        query.setLabel(fetchValue(ExporterConst.QUERY_LABEL));
        query.setDescription(fetchValue(ExporterConst.QUERY_DESCRIPTION));
        query.setContactId(fetchValue(ExporterConst.QUERY_CONTACT_ID));
        query.setExpirationDate(fetchValue(ExporterConst.QUERY_EXPIRATION_DATE,LocalDate.class));
        query.setCreatedAt(Instant.now());
        query.setDefaultTemplateId(fetchValue(ExporterConst.QUERY_DEFAULT_TEMPLATE_ID));
        query.setDefaultOutputFormat(fetchValue(ExporterConst.QUERY_DEFAULT_OUTPUT_FORMAT, Format.class));
        query.setContext(fetchValue(ExporterConst.QUERY_CONTEXT));

        return query;
    }

    private <T> T fetchValue(String key, Class<T> clazz) {
        String value = fetchValue(key);
        return (StringUtils.hasText(value)) ? (T) classConverterMap.get(clazz).convert(value) : null;
    }

    private String fetchValue(String key) {
        return parameters.get(key);
    }

    private interface Converter<T> {
        public T convert(String value);
    }


}
