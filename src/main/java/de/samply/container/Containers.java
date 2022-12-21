package de.samply.container;

import de.samply.template.ContainerTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Containers {

  private Map<ContainerTemplate, Map<String, Container>> templateIdContainerMap = new HashMap<>();

  public void addContainer(Container container) {
    Map<String, Container> idContainerMap = templateIdContainerMap.get(
        container.getContainerTemplate());
    if (idContainerMap == null) {
      idContainerMap = new HashMap<>();
      templateIdContainerMap.put(container.getContainerTemplate(), idContainerMap);
    }
    idContainerMap.put(container.getId(), container);
  }

  public Container getContainer(ContainerTemplate containerTemplate, String containerId) {
    Map<String, Container> idContainerMap = templateIdContainerMap.get(containerTemplate);
    return (idContainerMap != null) ? idContainerMap.get(containerId) : null;
  }

  public List<Container> getContainers(ContainerTemplate containerTemplate) {
    Map<String, Container> idContainerMap = templateIdContainerMap.get(containerTemplate);
    return (idContainerMap != null) ? new ArrayList<>(idContainerMap.values()) : new ArrayList<>();
  }

  public void addAttribute(ContainerTemplate containerTemplate, String containerId,
      Attribute attribute) {
    Container container = getContainer(containerTemplate, containerId);
    if (container == null) {
      container = new Container(containerId, containerTemplate);
      addContainer(container);
    }
    container.addAttribute(attribute);
  }

}
