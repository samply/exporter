package de.samply.core;

import de.samply.converter.Converter;
import de.samply.converter.ConverterManager;
import de.samply.query.Query;
import de.samply.query.QueryManager;
import de.samply.teiler.TeilerConst;
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
  private final QueryManager queryManager;

  public TeilerCore(
      @Autowired ConverterManager converterManager,
      @Autowired ConverterTemplateManager converterTemplateManager,
      @Autowired QueryManager queryManager
  ) {
    this.converterManager = converterManager;
    this.queryManager = queryManager;
    this.converterTemplateManager = converterTemplateManager;
  }

  public <O> Flux<O> retrieveQuery(TeilerParameters teilerParameters) throws TeilerCoreException {
    Errors errors = new Errors();

    // Fetch query.
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
      query = queryManager.fetchQuery(teilerParameters.queryId());
      if (query == null) {
        errors.addError("Query " + teilerParameters.queryId() + " not found");
      }
    } else {
      if (teilerParameters.query() == null) {
        errors.addError("Query not defined");
      } else {
        query = new Query(TeilerConst.DEFAULT_QUERY_ID, teilerParameters.query(),
            teilerParameters.queryFormat());
      }
    }

    // Fetch template.
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

    // Fetch converter.
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

    // Retrieve results if parameters are OK.
    if (errors.isEmpty()) {
      return retrieve(Flux.just(query.query()), converter, template);
    } else {
      throw new TeilerCoreException(errors.getMessages());
    }

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

  public <I, O> Flux<O> retrieve(Flux<I> inputFlux, Converter<I, O> converter,
      ConverterTemplate converterTemplate) {
    return converter.convert(inputFlux, converterTemplate);
  }


}
