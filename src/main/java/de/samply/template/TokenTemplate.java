package de.samply.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TokenTemplate {

    @JacksonXmlProperty(isAttribute = true, localName = "key")
    @JsonProperty("key")
    private String key;

    @JacksonXmlProperty(isAttribute = true, localName = "value")
    @JsonProperty("value")
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
