package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PermissionType {
  @JsonProperty("user")
  USER,
  @JsonProperty("group")
  GROUP
}
