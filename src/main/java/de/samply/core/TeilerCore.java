package de.samply.core;

import de.samply.converter.Converter;
import de.samply.converter.ConverterManager;
import de.samply.db.model.Query;
import de.samply.db.crud.TeilerDbService;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import java.io.IOException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class TeilerCore {


  private ConverterManager converterManager;
  private final ConverterTemplateManager converterTemplateManager;
  private final TeilerDbService teilerDbService;

  public TeilerCore(
      @Autowired ConverterManager converterManager,
      @Autowired ConverterTemplateManager converterTemplateManager,
      @Autowired TeilerDbService teilerDbService
  ) {
    this.converterManager = converterManager;
    this.teilerDbService = teilerDbService;
    this.converterTemplateManager = converterTemplateManager;
  }

  public <O> Flux<O> retrieveQuery(TeilerParameters teilerParameters) throws TeilerCoreException {
    Errors errors = new Errors();

    Query query = checkParametersAndFetchQuery(teilerParameters, errors);
    ConverterTemplate template = checkParametersAndFetchTemplate(teilerParameters, errors);
    Converter converter = checkParametersAndFetchConverter(teilerParameters, errors);

    if (errors.isEmpty()) {
      return retrieve(Flux.just(query.getQuery()), converter, template);
    } else {
      throw new TeilerCoreException(errors.getMessages());
    }

  }

  private Query checkParametersAndFetchQuery(TeilerParameters teilerParameters, Errors errors) {
    Query query = null;
    if (teilerParameters.queryFormat() == null) {
      errors.addError("Query format not provided");
    }
    if (teilerParameters.outputFormat() == null) {
      errors.addError("Output format not provided");
    }
    if (teilerParameters.sourceId() == null) {
      errors.addError("Source ID not provided");
    }
    if (teilerParameters.queryId() != null) {
      query = teilerDbService.fetchQuery(teilerParameters.queryId());
      if (query == null) {
        errors.addError("Query " + teilerParameters.queryId() + " not found");
      }
    } else {
      if (teilerParameters.query() == null) {
        errors.addError("Query not defined");
      } else {
        query = createTemporalQuery(teilerParameters);
      }
    }
    return query;

  }

  private Query createTemporalQuery(TeilerParameters teilerParameters) {
    Query query = new Query();
    query.setQuery(teilerParameters.query());
    query.setFormat(teilerParameters.queryFormat());
    return query;
  }

  private ConverterTemplate checkParametersAndFetchTemplate(TeilerParameters teilerParameters,
      Errors errors) {
    ConverterTemplate template = null;
    if (teilerParameters.templateId() != null) {
      template = converterTemplateManager.getConverterTemplate(teilerParameters.templateId());
      if (template == null) {
        errors.addError("Converter Template " + teilerParameters.templateId() + " not found");
      }
    } else {
      boolean fetchTemplate = true;
      if (teilerParameters.template() == null) {
        errors.addError("Template not defined");
        fetchTemplate = false;
      }
      if (teilerParameters.contentType() == null) {
        errors.addError("Content Type not defined");
        fetchTemplate = false;
      } else if (!(teilerParameters.contentType().equals(MediaType.APPLICATION_XML_VALUE)
          || teilerParameters.contentType().equals(MediaType.APPLICATION_JSON_VALUE))) {
        errors.addError("Content Type not supported (only XML or JSON are supported)");
        fetchTemplate = false;
      }
      if (fetchTemplate) {
        template = fetchTemplate(teilerParameters, errors);
      }
    }
    return template;
  }

  private ConverterTemplate fetchTemplate(TeilerParameters teilerParameters, Errors errors) {
    try {
      return converterTemplateManager.fetchConverterTemplate(teilerParameters.template(),
          teilerParameters.contentType());
    } catch (IOException e) {
      errors.addError("Error deserializing template");
      errors.addError(ExceptionUtils.getStackTrace(e));
      return null;
    }
  }

  private Converter checkParametersAndFetchConverter(TeilerParameters teilerParameters,
      Errors errors) {
    Converter converter = null;
    if (teilerParameters.queryFormat() != null && teilerParameters.outputFormat() != null
        && teilerParameters.sourceId() != null) {
      converter = converterManager.getBestMatchConverter(teilerParameters.queryFormat(),
          teilerParameters.outputFormat(),
          teilerParameters.sourceId());
      if (converter == null) {
        errors.addError(
            "No converter found for query format " + teilerParameters.queryFormat()
                + ", output format " + teilerParameters.outputFormat()
                + "and source id " + teilerParameters.sourceId());
      }
    }
    return converter;
  }

  public <I, O> Flux<O> retrieve(Flux<I> inputFlux, Converter<I, O> converter,
      ConverterTemplate converterTemplate) {
    return converter.convert(inputFlux, converterTemplate);
  }


}
