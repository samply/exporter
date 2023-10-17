package de.samply.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class CqlTemplate {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("token")
    private List<TokenTemplate> tokens = new ArrayList<>();

    @JacksonXmlProperty(localName = "measure-parameters")
    @JsonProperty("measure-parameters")
    private String measureParameters;

    @JacksonXmlProperty(localName = "default-fhir-search-query")
    @JsonProperty("default-fhir-search-query")
    private String defaultFhirSearchQuery;

    public List<TokenTemplate> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenTemplate> tokens) {
        this.tokens = tokens;
    }

    public String getMeasureParameters() {
        return measureParameters;
    }

    public void setMeasureParameters(String measureParameters) {
        this.measureParameters = measureParameters;
    }

    public String getDefaultFhirSearchQuery() {
        return defaultFhirSearchQuery;
    }

    public void setDefaultFhirSearchQuery(String defaultFhirSearchQuery) {
        this.defaultFhirSearchQuery = defaultFhirSearchQuery;
    }

}
