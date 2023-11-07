package de.samply.template.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.samply.template.AttributeTemplate;

import java.util.List;

public class AttributeTemplateLinks<T extends ContainerAttributeKeys> {

    protected final T containerAttributeKeys;
    @JsonIgnore
    private AttributeTemplate attributeTemplate;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("linking-attribute")
    private List<ContainerTemplateAttributeTemplate<T>> links;

    public AttributeTemplateLinks(T containerAttributeKeys, AttributeTemplate attributeTemplate, List<ContainerTemplateAttributeTemplate<T>> links) {
        this.containerAttributeKeys = containerAttributeKeys;
        this.attributeTemplate = attributeTemplate;
        this.links = links;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "attribute")
    @JsonProperty("attribute")
    public String getAttribute(){
        return containerAttributeKeys.getAttributeKey(attributeTemplate);
    }

    public List<ContainerTemplateAttributeTemplate<T>> getLinks() {
        return links;
    }

}
