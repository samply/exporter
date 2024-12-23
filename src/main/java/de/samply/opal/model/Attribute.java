package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attribute {

    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "namespace")
    private String namespace;
    @JsonProperty(value = "value")
    private String value;

}
