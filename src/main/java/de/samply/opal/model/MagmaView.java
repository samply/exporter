package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class MagmaView {

  @JsonProperty(value = "variables")
  private List<Variable> variables = new ArrayList<>();

  public List<Variable> getVariables() {
    return variables;
  }

  public void setVariables(List<Variable> variables) {
    this.variables = variables;
  }

}
