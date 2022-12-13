package de.samply.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.samply.converter.Format;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Source {

  @JacksonXmlProperty(isAttribute = true)
  @JsonProperty("id")
  private String id;
  @JacksonXmlProperty(isAttribute = true)
  @JsonProperty("url")
  private String url;
  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("query-format")
  private Set<Format> queryFormats = new HashSet<>();

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("result-format")
  private Set<Format> resultFormats = new HashSet<>();

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

  public Set<Format> getQueryFormats() {
    return queryFormats;
  }

  public void setQueryFormats(List<Format> queryFormats) {
    this.queryFormats.addAll(queryFormats);
  }

  public Set<Format> getResultFormats() {
    return resultFormats;
  }

  public void setResultFormats(List<Format> resultFormats) {
    this.resultFormats.addAll(resultFormats);
  }

}
