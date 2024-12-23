package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Category {

  @JsonProperty(value = "attributes")
  private List<Attribute> attributes = new ArrayList<>();
  @JsonProperty(value = "isMissing")
  private boolean isMissing = false;
  @JsonProperty(value = "name")
  private String name;

}
