package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attribute {

    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "namespace")
    private String namespace;
    @JsonProperty(value = "value")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
