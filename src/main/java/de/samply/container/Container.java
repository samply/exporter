package de.samply.container;

import de.samply.template.conversion.ContainerTemplate;
import java.util.ArrayList;
import java.util.List;

public class Container {

  private String id;
  private ContainerTemplate containerTemplate;
  private List<Attribute> attributes = new ArrayList<>();

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
    this.attributes.add(attribute);
  }

}
