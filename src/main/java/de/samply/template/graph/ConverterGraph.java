package de.samply.template.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class ConverterGraph<T extends ContainerAttributeKeys> {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("containers")
    private List<ContainerGraph<T>> containerGraphs;

    public ConverterGraph(List<ContainerGraph<T>> containerGraphs) {
        this.containerGraphs = containerGraphs;
    }

    public List<ContainerGraph<T>> getContainerGraphs() {
        return containerGraphs;
    }

}
