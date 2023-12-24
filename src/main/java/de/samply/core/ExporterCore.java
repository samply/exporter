package de.samply.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.samply.converter.Converter;
import de.samply.converter.ConverterManager;
import de.samply.db.crud.ExporterDbService;
import de.samply.db.model.Query;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import de.samply.template.token.TokenContext;
import de.samply.utils.Base64Utils;
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

        ConverterTemplate template = checkParametersAndFetchRequestTemplate(exporterParameters, errors);
        Query query = checkParametersAndFetchQuery(exporterParameters, template, errors);
        Converter converter = (template != null) ?
                checkParametersAndFetchConverter(exporterParameters, query, template, errors) : null;
        String queryExecutionContactId = exporterParameters.queryExecutionContactId();
        if (errors.isEmpty()) {
            return new ExporterCoreParameters(query, template, converter, queryExecutionContactId);
        } else {
            throw new ExporterCoreException(errors.getMessages());
        }
    }

    public <O> Flux<O> retrieveQuery(ExporterCoreParameters exporterCoreParameters, TokenContext tokenContext) throws ExporterCoreException {
        return retrieve(Flux.just(exporterCoreParameters.query().getQuery()), exporterCoreParameters.converter(), exporterCoreParameters.template(), tokenContext);
    }

    private Query checkParametersAndFetchQuery(ExporterParameters exporterParameters, ConverterTemplate template, Errors errors) {
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
            if (exporterParameters.query() != null) {
                query = createNewQuery(exporterParameters);
            } else {
                errors.addError("Query not defined");
            }
        }
        return query;
    }

    private Query createNewQuery(ExporterParameters exporterParameters) {
        Query query = new Query();
        query.setQuery(exporterParameters.query());
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

    private ConverterTemplate checkParametersAndFetchRequestTemplate(ExporterParameters exporterParameters,
                                                                     Errors errors) {
        ConverterTemplate converterTemplate = null;
        if (exporterParameters.templateId() != null) {
            converterTemplate = converterTemplateManager.getConverterTemplate(exporterParameters.templateId());
            if (converterTemplate == null) {
                errors.addError("Converter Template " + exporterParameters.templateId() + " not found");
            }
        }
        if (exporterParameters.templateId() == null || (exporterParameters.query() == null && exporterParameters.queryId() == null)) {
            boolean fetchTemplate = true;
            if (converterTemplate == null && exporterParameters.requestTemplate() == null) {
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
                converterTemplate = fetchConverterTemplate(exporterParameters, errors);
            }
        }
        if (converterTemplate == null) {
            errors.addError("Converter Template not defined");
        }
        return converterTemplate;
    }

    private ConverterTemplate fetchConverterTemplate(ExporterParameters exporterParameters, Errors errors) {
        try {
            return fetchConverterTemplate(exporterParameters.requestTemplate(), exporterParameters.contentType());
        } catch (IOException e) {
            errors.addError("Error deserializing template");
            errors.addError(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public ConverterTemplate fetchConverterTemplate(String template, String contentType)
            throws IOException {
        return ((contentType.equalsIgnoreCase(MediaType.APPLICATION_XML_VALUE)) ? new XmlMapper() : new ObjectMapper())
                .readValue(Base64Utils.decodeIfNecessary(template), ConverterTemplate.class);
    }

    private Converter checkParametersAndFetchConverter(ExporterParameters exporterParameters,
                                                       Query query, ConverterTemplate template, Errors errors) {
        Optional<Converter> converter = null;
        if (query != null && query.getFormat() != null && exporterParameters.outputFormat() != null
                && template != null) {
            converter = converterManager.getBestMatchConverter(query.getFormat(),
                    exporterParameters.outputFormat(), template.getSourceId(), template.getTargetId());
            if (converter.isEmpty()) {
                errors.addError(
                        "No converter found for query format " + exporterParameters.queryFormat()
                                + ", output format " + exporterParameters.outputFormat()
                                + "and source id " + template.getSourceId());
            }
        } else {
            errors.addError("No converter could be found");
        }
        return converter.get();
    }

    public <I, O> Flux<O> retrieve(Flux<I> inputFlux, Converter<I, O> converter, ConverterTemplate converterTemplate, TokenContext tokenContext) {
        return converter.convert(inputFlux, converterTemplate, tokenContext);
    }


}
