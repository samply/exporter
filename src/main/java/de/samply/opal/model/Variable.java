package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.samply.exporter.ExporterConst;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Variable {

  @JsonProperty(value = "entityType")
  private String entityType;
  @JsonProperty(value = "index")
  private Integer index;
  @JsonProperty(value = "isRepeatable")
  private boolean isRepeatable = false;
  @JsonProperty(value = "name")
  private String name;
  @JsonProperty(value = "valueType")
  private String valueType = ExporterConst.OPAL_DEFAULT_VALUE_TYPE;
  @JsonProperty(value = "mimeType")
  private String mimeType = "";
  @JsonProperty(value = "ocurrenceGroup")
  private String ocurrenceGroup = "";
  @JsonProperty(value = "referencedEntityType")
  private String referencedEntityType = "";
  @JsonProperty(value = "unit")
  private String unit = "";
  @JsonProperty(value = "attributes")
  private List<Attribute> attributes = new ArrayList<>();
  @JsonProperty(value = "categories")
  private List<Category> categories = new ArrayList<>();


}
