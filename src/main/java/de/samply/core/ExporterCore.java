package de.samply.core;

import de.samply.converter.Converter;
import de.samply.converter.ConverterManager;
import de.samply.db.crud.ExporterDbService;
import de.samply.db.model.Query;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
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

        Query query = checkParametersAndFetchQuery(exporterParameters, errors);
        ConverterTemplate template = checkParametersAndFetchTemplate(exporterParameters, errors);
        Converter converter =
                (template != null) ? checkParametersAndFetchConverter(exporterParameters, query, template,
                        errors) : null;
        if (errors.isEmpty()) {
            return new ExporterCoreParameters(query, template, converter);
        } else {
            throw new ExporterCoreException(errors.getMessages());
        }
    }

    public <O> Flux<O> retrieveQuery(ExporterCoreParameters exporterCoreParameters, TokenContext tokenContext) throws ExporterCoreException {
        return retrieve(Flux.just(exporterCoreParameters.query().getQuery()), exporterCoreParameters.converter(), exporterCoreParameters.template(), tokenContext);
    }

    private Query checkParametersAndFetchQuery(ExporterParameters exporterParameters, Errors errors) {
        Query query = null;
        if (exporterParameters.queryId() == null && exporterParameters.queryFormat() == null) {
            errors.addError("Query id nor query format not provided");
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
            if (exporterParameters.query() == null) {
                errors.addError("Query not defined");
            } else {
                query = createNewQuery(exporterParameters);
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
        Long queryId = exporterDbService.saveQueryAndGetQueryId(query);
        query.setId(queryId);
        return query;
    }

    private ConverterTemplate checkParametersAndFetchTemplate(ExporterParameters exporterParameters,
                                                              Errors errors) {
        ConverterTemplate template = null;
        if (exporterParameters.templateId() != null) {
            template = converterTemplateManager.getConverterTemplate(exporterParameters.templateId());
            if (template == null) {
                errors.addError("Converter Template " + exporterParameters.templateId() + " not found");
            }
        } else {
            boolean fetchTemplate = true;
            if (exporterParameters.template() == null) {
                errors.addError("Template not defined");
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
                template = fetchTemplate(exporterParameters, errors);
            }
        }
        return template;
    }

    private ConverterTemplate fetchTemplate(ExporterParameters exporterParameters, Errors errors) {
        try {
            return converterTemplateManager.fetchConverterTemplate(exporterParameters.template(),
                    exporterParameters.contentType());
        } catch (IOException e) {
            errors.addError("Error deserializing template");
            errors.addError(ExceptionUtils.getStackTrace(e));
            return null;
        }
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
