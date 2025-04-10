package de.samply.template.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;

public class ContainerAttribute<T extends ContainerAttributeKeys> {

    private final T containerAttributeKeys;
    @JsonIgnore
    private ContainerTemplate containerTemplate;

    @JsonIgnore
    private AttributeTemplate attributeTemplate;

    public ContainerAttribute(T containerAttributeKeys, ContainerTemplate containerTemplate, AttributeTemplate attributeTemplate) {
        this.containerAttributeKeys = containerAttributeKeys;
        this.containerTemplate = containerTemplate;
        this.attributeTemplate = attributeTemplate;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "container")
    @JsonProperty("container")
    public String getContainer() {
        return containerAttributeKeys.getContainerKey(containerTemplate);
    }

    @JacksonXmlProperty(isAttribute = true, localName = "attribute")
    @JsonProperty("attribute")
    public String getAttribute() {
        return containerAttributeKeys.getAttributeKey(attributeTemplate);
    }

    @JsonIgnore
    public ContainerTemplate getContainerTemplate() {
        return containerTemplate;
    }

    @JsonIgnore
    public AttributeTemplate getAttributeTemplate() {
        return attributeTemplate;
    }

}
