package de.samply.template.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.samply.template.ContainerTemplate;

import java.util.List;

public class ContainerTemplateGraph<T extends ContainerAttributeKeys>   {

    @JsonIgnore
    private final T containerAttributeKeys;
    @JsonIgnore
    private ContainerTemplate containerTemplate;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("linked-attribute")
    private List<AttributeTemplateLinks<T>> links;

    public ContainerTemplateGraph(T containerAttributeKeys, ContainerTemplate containerTemplate, List<AttributeTemplateLinks<T>> links) {
        this.containerAttributeKeys = containerAttributeKeys;
        this.containerTemplate = containerTemplate;
        this.links = links;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "container")
    @JsonProperty("container")
    public String getContainer(){
        return containerAttributeKeys.getContainerKey(containerTemplate);
    }

    public List<AttributeTemplateLinks<T>> getLinks() {
        return links;
    }

}
