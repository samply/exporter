package de.samply.template.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.samply.template.AttributeTemplate;

import java.util.ArrayList;
import java.util.List;

public class AttributeAndLinkAttributes<T extends ContainerAttributeKeys> {

    protected final T containerAttributeKeys;
    @JsonIgnore
    private AttributeTemplate attributeTemplate;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("linking-attributes")
    private List<ContainerAttribute<T>> linkingAttributes = new ArrayList<>();

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("linked-attributes")
    private List<ContainerAttribute<T>> linkedAttributes = new ArrayList<>();


    public AttributeAndLinkAttributes(T containerAttributeKeys, AttributeTemplate attributeTemplate) {
        this.containerAttributeKeys = containerAttributeKeys;
        this.attributeTemplate = attributeTemplate;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "reference-attribute")
    @JsonProperty("reference-attribute")
    public String getAttribute() {
        return containerAttributeKeys.getAttributeKey(attributeTemplate);
    }

    public List<ContainerAttribute<T>> getLinkingAttributes() {
        return linkingAttributes;
    }

    public void setLinkingAttributes(List<ContainerAttribute<T>> linkingAttributes) {
        if (linkingAttributes != null) {
            this.linkingAttributes = linkingAttributes;
        }
    }

    public List<ContainerAttribute<T>> getLinkedAttributes() {
        return linkedAttributes;
    }

    public void setLinkedAttributes(List<ContainerAttribute<T>> linkedAttributes) {
        if (linkedAttributes != null) {
            this.linkedAttributes = linkedAttributes;
        }
    }

}
