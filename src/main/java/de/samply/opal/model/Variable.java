package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.samply.exporter.ExporterConst;
import java.util.ArrayList;
import java.util.List;

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


  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public boolean getIsRepeatable() {
    return isRepeatable;
  }

  public void setIsRepeatable(boolean repeatable) {
    isRepeatable = repeatable;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValueType() {
    return valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getOcurrenceGroup() {
    return ocurrenceGroup;
  }

  public void setOcurrenceGroup(String ocurrenceGroup) {
    this.ocurrenceGroup = ocurrenceGroup;
  }

  public String getReferencedEntityType() {
    return referencedEntityType;
  }

  public void setReferencedEntityType(String referencedEntityType) {
    this.referencedEntityType = referencedEntityType;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public List<Category> getCategories() {
    return categories;
  }

  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }

}
