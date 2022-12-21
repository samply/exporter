package de.samply.container;

import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    this.attributeTemplateAttributeMap.put(attribute.getAttributeTemplate(), attribute);
  }

  public boolean containsAttributeTemplate(AttributeTemplate attributeTemplate) {
    return attributeTemplateAttributeMap.keySet().contains(attributeTemplate);
  }

  public boolean containsAttribute (Attribute attribute){
    String attributeValue = getAttributeValue(attribute.getAttributeTemplate());
    return (attributeValue != null) ? attributeValue.equals(attribute.getValue()) : false;
  }

  public String getAttributeValue (AttributeTemplate attributeTemplate){
    Attribute attribute = attributeTemplateAttributeMap.get(attributeTemplate);
    return (attribute != null) ? attribute.getValue() : null;
  }

  @Override
  public Container clone() {
    Container result = new Container(id, containerTemplate);
    attributeTemplateAttributeMap.values().forEach(attribute -> result.addAttribute(attribute));
    return result;
  }

  public Container cloneAndReplaceAttribute (Attribute attribute){
    Container result = clone();
    result.addAttribute(attribute);
    return result;
  }

}
