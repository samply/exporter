package de.samply.ldm;

import org.springframework.stereotype.Component;

@Component
public class LdmClient {
/*
  private final ConverterTemplateManager converterTemplateManager;
  private final ConversionTemplateManager conversionTemplateManager;
  private final QueryManager queryManager;

  public LdmClient(
      @Autowired ConverterTemplateManager converterTemplateManager,
      @Autowired QueryManager queryManager,
      @Autowired ConversionTemplateManager conversionTemplateManager) {
    this.conversionTemplateManager = converterTemplateManager;
    this.queryManager = queryManager;
    this.conversionTemplateManager = conversionTemplateManager;
  }

  public String retrieve(String sourceId, String queryId, String resultTemplateId,
      Format resultFormat) throws LdmClientException {
    Query query = queryManager.fetchQuery(queryId);
    if (query != null) {
      return retrieve(sourceId, query.query(), query.queryFormat(), resultTemplateId, resultFormat);
    } else {
      throw new LdmClientException("Query " + queryId + " not found");
    }
  }

  public String retrieve(String sourceId, String query, Format queryFormat,
      String resultTemplateId, Format resultFormat) throws LdmClientException {
    ConverterTemplate converterTemplate = conversionTemplateManager.getSource(sourceId);
    if (converterTemplate == null) {
      throw new LdmClientException("Source " + sourceId + " not found");
    }
    if (!converterTemplate.getQueryFormats().contains(queryFormat)) {
      throw new LdmClientException(
          "Query Format " + queryFormat + " not defined for source " + sourceId);
    }
    //TODO
    return null;
  }
*/

}
