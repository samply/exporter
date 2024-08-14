package de.samply.container;

import de.samply.fhir.ContainerAttributesFhirDependencies;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Containers {

    private Map<ContainerTemplate, Map<String, List<Container>>> templateIdContainerMap = new HashMap<>();
    private Map<ContainerTemplate, ContainerAttributesFhirDependencies> templatIdFhirDependencies = new HashMap<>();

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
        ContainerAttributesFhirDependencies containerAttributesFhirDependencies = templatIdFhirDependencies.get(container.getContainerTemplate());
        if (containerAttributesFhirDependencies == null) {
            templatIdFhirDependencies.put(container.getContainerTemplate(), new ContainerAttributesFhirDependencies(container.getContainerTemplate()));
        }

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
        //TODO: Add fhir dependencies logic here
        if (!containsAttributeTemplate(containers, attribute.attributeTemplate())) {
            containers.forEach(container -> container.addAttribute(attribute));
        } else if (!containsAttribute(containers, attribute)) {
            List.copyOf(containers).forEach(container -> containers.add(container.cloneAndReplaceAttribute(attribute)));
            removeDuplicates(containers);
        }
    }

    private void removeDuplicates(List<Container> containers) {
        if (containers.size() > 1) {
            List<Container> tempContainers = List.copyOf(containers).stream().distinct().collect(Collectors.toList());
            if (containers.size() != tempContainers.size()) {
                containers.clear();
                containers.addAll(tempContainers);
            }
        }
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
