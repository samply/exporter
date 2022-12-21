package de.samply.container;

import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Container {

  private String id;
  private ContainerTemplate containerTemplate;
  private List<Attribute> attributes = new ArrayList<>();
  private Map<AttributeTemplate, Integer> attributeTemplatePositionMap = new HashMap<>();

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
    return attributes;
  }

  public void addAttribute(Attribute attribute) {
    this.attributeTemplatePositionMap.put(attribute.getAttributeTemplate(), this.attributes.size());
    this.attributes.add(attribute);
  }

  public boolean containsAttributeTemplate(AttributeTemplate attributeTemplate) {
    return attributeTemplatePositionMap.keySet().contains(attributeTemplate);
  }

  public boolean containsAttribute (Attribute attribute){
    String attributeValue = getAttributeValue(attribute.getAttributeTemplate());
    return (attributeValue != null) ? attributeValue.equals(attribute.getValue()) : false;
  }

  public String getAttributeValue (AttributeTemplate attributeTemplate){
    Integer index = attributeTemplatePositionMap.get(attributeTemplate);
    return (index != null) ? attributes.get(index).getValue() : null;
  }

  public void replaceAttribute(Attribute attribute){
    Integer index = attributeTemplatePositionMap.get(attribute.getAttributeTemplate());
    if (index == null){
      addAttribute(attribute);
    } else {
      attributes.add(index, attribute);
    }
  }

  @Override
  public Container clone() {
    Container result = new Container(id, containerTemplate);
    attributes.forEach(attribute -> result.addAttribute(attribute));
    return result;
  }

  public Container cloneAndReplaceAttribute (Attribute attribute){
    Container result = clone();
    result.replaceAttribute(attribute);
    return result;
  }

}
