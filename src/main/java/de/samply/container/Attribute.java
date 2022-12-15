package de.samply.container;

import de.samply.template.AttributeTemplate;

public class Attribute {

  // If isContainerRef is true, then type is the container-type.
  private AttributeTemplate attributeTemplate;
  private String value;

  public Attribute(AttributeTemplate attributeTemplate, String value) {
    this.attributeTemplate = attributeTemplate;
    this.value = value;
  }

  public AttributeTemplate getAttributeTemplate() {
    return attributeTemplate;
  }

  public String getValue() {
    return value;
  }

}
