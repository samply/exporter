package de.samply.container;

import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Containers {

  private Map<ContainerTemplate, Map<String, List<Container>>> templateIdContainerMap = new HashMap<>();

  public void addContainer(Container container) {
    Map<String, List<Container>> idContainerMap = templateIdContainerMap.get(
        container.getContainerTemplate());
    if (idContainerMap == null) {
      idContainerMap = new HashMap<>();
      templateIdContainerMap.put(container.getContainerTemplate(), idContainerMap);
    }
    List<Container> containerList = idContainerMap.get(container.getId());
    if (containerList == null) {
      containerList = new ArrayList<>();
      idContainerMap.put(container.getId(), containerList);
    }
    containerList.add(container);
  }

  public List<Container> getContainer(ContainerTemplate containerTemplate, String containerId) {
    Map<String, List<Container>> idContainerMap = templateIdContainerMap.get(containerTemplate);
    return (idContainerMap != null) ? idContainerMap.get(containerId) : null;
  }

  public List<Container> getContainers(ContainerTemplate containerTemplate) {
    Map<String, List<Container>> idContainerMap = templateIdContainerMap.get(containerTemplate);
    return (idContainerMap != null) ? new ArrayList<>(idContainerMap.values().stream().flatMap(
        List::stream).collect(Collectors.toList())) : new ArrayList<>();
  }

  public void addAttribute(ContainerTemplate containerTemplate, String containerId,
      Attribute attribute) {
    List<Container> containers = getContainer(containerTemplate, containerId);
    if (containers == null) {
      Container container = new Container(containerId, containerTemplate);
      addContainer(container);
      container.addAttribute(attribute);
    } else {
      addAttribute(containers, attribute);
    }
  }

  private void addAttribute(List<Container> containers, Attribute attribute) {
    if (!containsAttributeTemplate(containers, attribute.getAttributeTemplate())) {
      containers.forEach(container -> container.addAttribute(attribute));
    } else if (!containsAttribute(containers, attribute)) {
      fetchSubgroupOfContainersWithAttributeTemplateAndSameAttributeValue(containers,
          attribute.getAttributeTemplate()).forEach(
          container -> containers.add(container.cloneAndReplaceAttribute(attribute)));
    }
  }

  private List<Container> fetchSubgroupOfContainersWithAttributeTemplateAndSameAttributeValue(
      List<Container> containers, AttributeTemplate attributeTemplate) {
    List<Container> results = new ArrayList<>();
    String attributeValue = containers.get(0).getAttributeValue(attributeTemplate);
    containers.forEach(container -> {
      if (container.getAttributeValue(attributeTemplate).equals(attributeValue)) {
        results.add(container);
      }
    });
    return results;
  }

  private boolean containsAttributeTemplate(List<Container> containers,
      AttributeTemplate attributeTemplate) {
    return containers.get(0).containsAttributeTemplate(attributeTemplate);
  }

  private boolean containsAttribute(List<Container> containers, Attribute attribute) {
    for (Container container : containers) {
      if (container.containsAttribute(attribute)) {
        return true;
      }
    }
    return false;
  }

}
