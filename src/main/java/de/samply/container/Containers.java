package de.samply.container;

import de.samply.fhir.BasicContainerAttributesComparator;
import de.samply.fhir.ContainerAttributesComparator;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Containers {

    private final ContainerAttributesComparator containerAttributesComparator;
    private Map<ContainerTemplate, Map<String, List<Container>>> templateIdContainerMap = new HashMap<>();

    public Containers() {
        containerAttributesComparator = new BasicContainerAttributesComparator();
    }

    public Containers(ContainerAttributesComparator containerAttributesComparator) {
        this.containerAttributesComparator = containerAttributesComparator;
    }


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
            filterContainersToAddAttribute(containers, attribute).forEach(container -> {
                container.addAttribute(attribute);
                // If the container is a clone of another container
                if (!containers.contains(container)) {
                    containers.add(container);
                }
            });
        }
    }

    private List<Container> filterContainersToAddAttribute(List<Container> containers, Attribute attribute) {
        List<Container> result = new ArrayList<>();
        AtomicBoolean isContainerCloned = new AtomicBoolean(false);
        containers.stream().forEach(container -> {
            List<List<AttributeTemplate>> attributeTemplates = containerAttributesComparator.fetchIncompatibleCurrentAttributesOfContainerWithNewAttributes(attribute, container);
            if (attributeTemplates.size() > 0) {
                attributeTemplates.forEach(tempAttributeTemplates -> {
                    Container clonedContainer = container.clone();
                    tempAttributeTemplates.forEach(clonedContainer::removeAttribute);
                    result.add(clonedContainer);
                    isContainerCloned.set(true);
                });
            } else {
                if (container.containsAttributeTemplate(attribute.attributeTemplate())) {
                    isContainerCloned.set(true);
                    result.add(container.clone());
                } else {
                    result.add(container);
                }
            }
        });
        if (isContainerCloned.get()) {
            removeDuplicates(containers);
        }
        return result;
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

}
