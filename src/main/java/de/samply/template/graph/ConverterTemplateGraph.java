package de.samply.template.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class ConverterTemplateGraph<T extends ContainerAttributeKeys> {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("containers")
    private List<ContainerTemplateGraph<T>> containerTemplateGraphs;

    public ConverterTemplateGraph(List<ContainerTemplateGraph<T>> containerTemplateGraphs) {
        this.containerTemplateGraphs = containerTemplateGraphs;
    }

    public List<ContainerTemplateGraph<T>> getContainerTemplateGraphs() {
        return containerTemplateGraphs;
    }

}
