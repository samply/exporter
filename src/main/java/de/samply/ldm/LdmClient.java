package de.samply.ldm;

import de.samply.converter.Converter;
import de.samply.converter.ConverterManager;
import de.samply.converter.Format;
import de.samply.query.Query;
import de.samply.query.QueryManager;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class LdmClient {

  private ConverterManager converterManager;
  private final ConverterTemplateManager converterTemplateManager;
  private final QueryManager queryManager;

  public LdmClient(
      @Autowired ConverterManager converterManager,
      @Autowired ConverterTemplateManager converterTemplateManager,
      @Autowired QueryManager queryManager
  ) {
    this.converterManager = converterManager;
    this.queryManager = queryManager;
    this.converterTemplateManager = converterTemplateManager;
  }

  public <O> Flux<O> retrieveByQueryId(String sourceId, String queryId, Format outputFormat,
      String converterTemplateId) throws LdmClientException {
    Query query = queryManager.fetchQuery(queryId);
    if (query == null) {
      throw new LdmClientException("Query " + queryId + " not found");
    }
    return retrieveByQuery(sourceId, query.query(), query.queryFormat(), outputFormat,
        converterTemplateId);
  }

  public <O> Flux<O> retrieveByQuery(String sourceId, String query, Format queryFormat,
      Format outputFormat, String converterTemplateId) throws LdmClientException {
    return retrieve(sourceId, Flux.just(query), queryFormat, outputFormat, converterTemplateId);
  }

  public <I, O> Flux<O> retrieve(String sourceId, Flux<I> inputFlux, Format inputFormat,
      Format outputFormat, String converterTemplateId) throws LdmClientException {
    Converter converter = converterManager.getBestMatchConverter(inputFormat, outputFormat,
        sourceId);
    if (converter == null) {
      throw new LdmClientException(
          "No converter found for input format " + inputFormat + ", output format "
              + outputFormat + "and source id " + sourceId);
    }
    return retrieve(inputFlux, converter, converterTemplateId);
  }

  public <I, O> Flux<O> retrieve(Flux<I> inputFlux, Format inputFormat, Format outputFormat,
      String converterTemplateId) throws LdmClientException {
    Converter converter = converterManager.getBestMatchConverter(inputFormat, outputFormat);
    if (converter == null) {
      throw new LdmClientException(
          "No converter found for input format " + inputFormat + " and output format "
              + outputFormat);
    }
    return retrieve(inputFlux, converter, converterTemplateId);
  }

  public <I, O> Flux<O> retrieve(Flux<I> inputFlux, Converter<I, O> converter,
      String converterTemplateId) throws LdmClientException {
    ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate(
        converterTemplateId);
    if (converterTemplate == null) {
      throw new LdmClientException("Converter Template " + converterTemplateId + " not found");
    }
    return converter.convert(inputFlux, converterTemplate);
  }


}
