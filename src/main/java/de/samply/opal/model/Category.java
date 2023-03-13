package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Category {

  @JsonProperty(value = "attributes")
  private List<Attribute> attributes = new ArrayList<>();
  @JsonProperty(value = "isMissing")
  private boolean isMissing = false;
  @JsonProperty(value = "name")
  private String name;

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public boolean isMissing() {
    return isMissing;
  }

  public void setMissing(boolean missing) {
    isMissing = missing;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
