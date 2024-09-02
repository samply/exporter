package de.samply.container;

import de.samply.fhir.BasicContainerAttributesComparator;
import de.samply.fhir.ContainerAttributesComparator;
import de.samply.template.ContainerTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        containers.stream().filter(container -> containerAttributesComparator.belongsAttributeToContainer(attribute, container)).forEach(container ->
                // If the container already has the value, it is created a duplicate of the container
                result.add(container.containsAttributeTemplate(attribute.attributeTemplate()) ? container.clone() : container)
        );
        return result;
    }

    /*
    private boolean isContainerCompatibleWithAttribute(
            Container container, Attribute attribute, List<AttributeTemplate> attributeDependencies) {
        if (attributeDependencies == null || attributeDependencies.size() == 0) {
            return true;
        }

        //TODO
        return true;
    }

    private List<AttributeTemplate> fetchAttributeTemplateDependencies(List<Container> containers, Attribute attribute) {
        if (containers.size() > 0) {
            ContainerAttributesFhirDependencies containerAttributesFhirDependencies =
                    templatIdFhirDependencies.get(containers.get(0).getContainerTemplate());
            return containerAttributesFhirDependencies.getFhirDependencies(attribute.attributeTemplate());
        }
        return new ArrayList<>();
    }
     */

    /**
     * This method is called for a list of containers with at least one container with one attribute
     * (@see {@link Containers#addAttribute(ContainerTemplate, String, Attribute)})
     *
     * @param containers
     * @param attribute
     */
    /*
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
        for (Container container : containers) {
            if (container.containsAttributeTemplate(attributeTemplate)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAttribute(List<Container> containers, Attribute attribute) {
        for (Container container : containers) {
            if (container.containsAttribute(attribute)) {
                return true;
            }
        }
        return false;
    }
*/
}
