package de.samply.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "request")
public class RequestTemplate {

    @JacksonXmlProperty(isAttribute = true, localName = "query")
    @JsonProperty("query")
    private String query;


    @JacksonXmlProperty(isAttribute = true, localName = "template")
    @JsonProperty("template")
    private ConverterTemplate converterTemplate;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ConverterTemplate getConverterTemplate() {
        return converterTemplate;
    }

    public void setConverterTemplate(ConverterTemplate converterTemplate) {
        this.converterTemplate = converterTemplate;
    }

}
