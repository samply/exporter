package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Permission {
  @JsonProperty("administrate")
  ADMINISTRATE,
  @JsonProperty("use")
  USE;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
