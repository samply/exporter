package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Permission {
  @JsonProperty("administrate")
  PROJECT_ALL,
  @JsonProperty("use")
  USE;

  @Override
  public String toString() {
    return super.toString();
  }
}
