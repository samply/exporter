package de.samply.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.samply.converter.Converter;
import de.samply.converter.ConverterManager;
import de.samply.db.crud.ExporterDbService;
import de.samply.db.model.Query;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import de.samply.template.RequestTemplate;
import de.samply.template.token.TokenContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Component
public class ExporterCore {


    private ConverterManager converterManager;
    private final ConverterTemplateManager converterTemplateManager;
    private final ExporterDbService exporterDbService;

    public ExporterCore(
            @Autowired ConverterManager converterManager,
            @Autowired ConverterTemplateManager converterTemplateManager,
            @Autowired ExporterDbService exporterDbService
    ) {
        this.converterManager = converterManager;
        this.exporterDbService = exporterDbService;
        this.converterTemplateManager = converterTemplateManager;
    }

    public ExporterCoreParameters extractParameters(ExporterParameters exporterParameters)
            throws ExporterCoreException {
        Errors errors = new Errors();

        RequestTemplate template = checkParametersAndFetchRequestTemplate(exporterParameters, errors);
        Query query = checkParametersAndFetchQuery(exporterParameters, template, errors);
        Converter converter =
                (template != null) ? checkParametersAndFetchConverter(exporterParameters, query, template.getConverterTemplate(),
                        errors) : null;
        if (errors.isEmpty()) {
            return new ExporterCoreParameters(query, template.getConverterTemplate(), converter);
        } else {
            throw new ExporterCoreException(errors.getMessages());
        }
    }

    public <O> Flux<O> retrieveQuery(ExporterCoreParameters exporterCoreParameters, TokenContext tokenContext) throws ExporterCoreException {
        return retrieve(Flux.just(exporterCoreParameters.query().getQuery()), exporterCoreParameters.converter(), exporterCoreParameters.template(), tokenContext);
    }

    private Query checkParametersAndFetchQuery(ExporterParameters exporterParameters, RequestTemplate template, Errors errors) {
        Query query = null;
        if (exporterParameters.queryId() == null && exporterParameters.queryFormat() == null) {
            errors.addError("Query format not provided");
        }
        if (exporterParameters.outputFormat() == null) {
            errors.addError("Output format not provided");
        }
        if (exporterParameters.queryId() != null) {
            Optional<Query> queryOptional = exporterDbService.fetchQuery(exporterParameters.queryId());
            if (queryOptional.isEmpty()) {
                errors.addError("Query " + exporterParameters.queryId() + " not found");
            } else {
                query = queryOptional.get();
            }
        } else {
            if (exporterParameters.query() != null || template.getQuery() != null){
                query = createNewQuery(exporterParameters, template);
            } else {
                errors.addError("Query not defined");
            }
        }
        return query;

    }

    private Query createNewQuery(ExporterParameters exporterParameters, RequestTemplate template) {
        Query query = new Query();
        String sQuery = null;
        if (exporterParameters.query() != null){
            sQuery = exporterParameters.query();
        } else if (template != null && template.getQuery() != null){
            sQuery = template.getQuery();
        }
        query.setQuery(sQuery);
        query.setFormat(exporterParameters.queryFormat());
        query.setCreatedAt(Instant.now());
        query.setLabel(exporterParameters.queryLabel());
        query.setDescription(exporterParameters.queryDescription());
        query.setContactId(exporterParameters.queryContactId());
        query.setExpirationDate(exporterParameters.queryExpirationDate());
        query.setContext(exporterParameters.queryContext());
        query.setDefaultOutputFormat(exporterParameters.outputFormat());
        query.setDefaultTemplateId(exporterParameters.templateId());
        Long queryId = exporterDbService.saveQueryAndGetQueryId(query);
        query.setId(queryId);
        return query;
    }

    private RequestTemplate checkParametersAndFetchRequestTemplate(ExporterParameters exporterParameters,
                                                                   Errors errors) {
        RequestTemplate result = null;
        if (exporterParameters.templateId() != null) {
            ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate(exporterParameters.templateId());
            if (result == null) {
                errors.addError("Converter Template " + exporterParameters.templateId() + " not found");
            } else {
                result = new RequestTemplate();
                result.setConverterTemplate(converterTemplate);
                result.setQuery(exporterParameters.query());
            }
        }
        if (exporterParameters.templateId() == null || (exporterParameters.query() == null && exporterParameters.queryId() == null)){
            boolean fetchTemplate = true;
            if (exporterParameters.requestTemplate() == null) {
                errors.addError("Request Template not defined");
                fetchTemplate = false;
            }
            if (exporterParameters.contentType() == null) {
                errors.addError("Content Type not defined");
                fetchTemplate = false;
            } else if (!(exporterParameters.contentType().equals(MediaType.APPLICATION_XML_VALUE)
                    || exporterParameters.contentType().equals(MediaType.APPLICATION_JSON_VALUE))) {
                errors.addError("Content Type not supported (only XML or JSON are supported)");
                fetchTemplate = false;
            }
            if (fetchTemplate) {
                result = fetchRequestTemplate(exporterParameters, errors);
            }
        }
        if (result.getConverterTemplate() == null){
            errors.addError("Converter Template not defined");
        }
        return result;
    }

    private RequestTemplate fetchRequestTemplate(ExporterParameters exporterParameters, Errors errors) {
        try {
            return fetchRequestTemplate(exporterParameters.requestTemplate(), exporterParameters.contentType());
        } catch (IOException e) {
            errors.addError("Error deserializing template");
            errors.addError(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public RequestTemplate fetchRequestTemplate(String template, String contentType)
            throws IOException {
        return ((contentType.equalsIgnoreCase(MediaType.APPLICATION_XML_VALUE)) ? new XmlMapper() : new ObjectMapper())
                .readValue(template, RequestTemplate.class);
    }

    private Converter checkParametersAndFetchConverter(ExporterParameters exporterParameters,
                                                       Query query, ConverterTemplate template, Errors errors) {
        Converter converter = null;
        if (query != null && query.getFormat() != null && exporterParameters.outputFormat() != null
                && template != null) {
            converter = converterManager.getBestMatchConverter(query.getFormat(),
                    exporterParameters.outputFormat(), template.getSourceId(), template.getTargetId());
            if (converter == null) {
                errors.addError(
                        "No converter found for query format " + exporterParameters.queryFormat()
                                + ", output format " + exporterParameters.outputFormat()
                                + "and source id " + template.getSourceId());
            }
        } else {
            errors.addError("No converter could be found");
        }
        return converter;
    }

    public <I, O> Flux<O> retrieve(Flux<I> inputFlux, Converter<I, O> converter, ConverterTemplate converterTemplate, TokenContext tokenContext) {
        return converter.convert(inputFlux, converterTemplate, tokenContext);
    }


}
