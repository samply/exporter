package de.samply.ldm;

import de.samply.query.Query;
import de.samply.query.QueryManager;
import de.samply.result.ResultFormat;
import de.samply.result.container.template.ResultTemplateManager;
import de.samply.query.QueryFormat;
import de.samply.source.Source;
import de.samply.source.SourcesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LdmClient {

  private final SourcesManager sourcesManager;
  private final ResultTemplateManager resultTemplateManager;
  private final QueryManager queryManager;

  public LdmClient(
      @Autowired SourcesManager sourcesManager,
      @Autowired QueryManager queryManager,
      @Autowired ResultTemplateManager resultTemplateManager) {
    this.sourcesManager = sourcesManager;
    this.queryManager = queryManager;
    this.resultTemplateManager = resultTemplateManager;
  }

  public String retrieve(String sourceId, String queryId, String resultTemplateId,
      ResultFormat resultFormat) throws LdmClientException {
    Query query = queryManager.fetchQuery(queryId);
    if (query != null) {
      return retrieve(sourceId, query.query(), query.queryFormat(), resultTemplateId, resultFormat);
    } else {
      throw new LdmClientException("Query " + queryId + " not found");
    }
  }

  public String retrieve(String sourceId, String query, QueryFormat queryFormat,
      String resultTemplateId, ResultFormat resultFormat) throws LdmClientException {
    Source source = sourcesManager.getSource(sourceId);
    if (source == null) {
      throw new LdmClientException("Source " + sourceId + " not found");
    }
    if (!source.getQueryFormats().contains(queryFormat)) {
      throw new LdmClientException(
          "Query Format " + queryFormat + " not defined for source " + sourceId);
    }
    //TODO
    return null;
  }


}
