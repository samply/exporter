package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Table {

  @JsonProperty(value = "name")
  private String name;
  @JsonProperty(value = "entityType")
  private String entityType;

  @JsonProperty(value = "variables")
  private List<Variable> variables = new ArrayList<>();

}
