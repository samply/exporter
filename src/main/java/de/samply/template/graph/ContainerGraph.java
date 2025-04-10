package de.samply.template.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.samply.template.ContainerTemplate;

import java.util.List;

public class ContainerGraph<T extends ContainerAttributeKeys>   {

    @JsonIgnore
    private final T containerAttributeKeys;
    @JsonIgnore
    private ContainerTemplate containerTemplate;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("links")
    private List<AttributeAndLinkAttributes<T>> links;

    public ContainerGraph(T containerAttributeKeys, ContainerTemplate containerTemplate, List<AttributeAndLinkAttributes<T>> links) {
        this.containerAttributeKeys = containerAttributeKeys;
        this.containerTemplate = containerTemplate;
        this.links = links;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "reference-container")
    @JsonProperty("reference-container")
    public String getContainer(){
        return containerAttributeKeys.getContainerKey(containerTemplate);
    }

    public List<AttributeAndLinkAttributes<T>> getLinks() {
        return links;
    }

}
