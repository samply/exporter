package de.samply.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.samply.query.QueryFormat;
import de.samply.result.ResultFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Source {

  @JacksonXmlProperty(isAttribute = true)@JsonProperty("id")
  private String id;
  @JacksonXmlProperty(isAttribute = true)@JsonProperty("url")
  private String url;
  @JacksonXmlElementWrapper(useWrapping = false) @JsonProperty("query-format")
  private Set<QueryFormat> queryFormats = new HashSet<>();

  @JacksonXmlElementWrapper(useWrapping = false) @JsonProperty("result-format")
  private Set<ResultFormat> resultFormats = new HashSet<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Set<QueryFormat> getQueryFormats() {
    return queryFormats;
  }

  public void setQueryFormats(List<QueryFormat> queryFormats) {
    this.queryFormats.addAll(queryFormats);
  }

  public Set<ResultFormat> getResultFormats() {
    return resultFormats;
  }

  public void setResultFormats(List<ResultFormat> resultFormats) {
    this.resultFormats.addAll(resultFormats);
  }

}
