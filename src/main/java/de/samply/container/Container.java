package de.samply.container;

import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;

import java.util.*;

public class Container {

    private String id;
    private ContainerTemplate containerTemplate;
    private Map<AttributeTemplate, Attribute> attributeTemplateAttributeMap = new HashMap<>();

    public Container(String id, ContainerTemplate containerTemplate) {
        this.id = id;
        this.containerTemplate = containerTemplate;
    }

    public String getId() {
        return id;
    }

    public ContainerTemplate getContainerTemplate() {
        return containerTemplate;
    }

    public List<Attribute> getAttributes() {
        return List.copyOf(attributeTemplateAttributeMap.values());
    }

    public void addAttribute(Attribute attribute) {
        this.attributeTemplateAttributeMap.put(attribute.attributeTemplate(), attribute);
    }

    public boolean containsAttributeTemplate(AttributeTemplate attributeTemplate) {
        return attributeTemplateAttributeMap.keySet().contains(attributeTemplate);
    }

    public boolean containsAttribute(Attribute attribute) {
        String attributeValue = getAttributeValue(attribute.attributeTemplate());
        return (attributeValue != null) ? attributeValue.equals(attribute.value()) : false;
    }

    public String getAttributeValue(AttributeTemplate attributeTemplate) {
        Attribute attribute = attributeTemplateAttributeMap.get(attributeTemplate);
        return (attribute != null) ? attribute.value() : null;
    }

    public Optional<Attribute> getAttribute(AttributeTemplate attributeTemplate) {
        return Optional.ofNullable(attributeTemplateAttributeMap.get(attributeTemplate));
    }

    @Override
    public Container clone() {
        Container result = new Container(id, containerTemplate);
        attributeTemplateAttributeMap.values().forEach(attribute -> result.addAttribute(attribute));
        return result;
    }

    public Container cloneAndReplaceAttribute(Attribute attribute) {
        Container result = clone();
        result.addAttribute(attribute);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        if (!Objects.equals(id, container.id) || containerTemplate != container.containerTemplate ||
                attributeTemplateAttributeMap.size() != container.attributeTemplateAttributeMap.size()) {
            return false;
        }
        return attributeTemplateAttributeMap.entrySet().stream().allMatch(attributeTemplateAttribute -> {
            String attributeValue = container.getAttributeValue(attributeTemplateAttribute.getKey());
            return attributeValue != null && attributeTemplateAttribute.getValue().value().equals(attributeValue);
        });
    }

}
