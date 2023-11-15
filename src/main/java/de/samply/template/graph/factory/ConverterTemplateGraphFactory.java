package de.samply.template.graph.factory;

import de.samply.converter.Format;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.template.graph.*;

import java.util.*;

public abstract class ConverterTemplateGraphFactory<T extends ContainerAttributeKeys> {

    private final T containerAttributeKeys;

    protected abstract Format getOutputFormat();

    public ConverterTemplateGraphFactory(T containerAttributeKeys) {
        this.containerAttributeKeys = containerAttributeKeys;
    }

    public ConverterGraph<T> create(ConverterTemplate converterTemplate) {
        return new ConverterGraph<>(createContainerTemplateGraphs(converterTemplate));
    }

    private List<ContainerGraph<T>> createContainerTemplateGraphs(ConverterTemplate converterTemplate) {
        List<ContainerGraph<T>> result = new ArrayList<>();
        Map<AttributeTemplate, List<ContainerAttribute<T>>> linkedAttributeLinkingAttributesMap = fetchLinkedAttributeLinkingAttributesMap(converterTemplate);
        Map<AttributeTemplate, List<ContainerAttribute<T>>> linkingAttributeLinkedAttributesMaps = fetchLinkingAttributeLinkedAttributesMap(converterTemplate, linkedAttributeLinkingAttributesMap);
        converterTemplate.getContainerTemplates().forEach(containerTemplate -> {
            result.add(new ContainerGraph<>(containerAttributeKeys, containerTemplate, createAttributeLinks(containerTemplate, linkedAttributeLinkingAttributesMap, linkingAttributeLinkedAttributesMaps)));
        });
        return result;
    }

    private List<AttributeAndLinkAttributes<T>> createAttributeLinks(ContainerTemplate containerTemplate,
                                                                     Map<AttributeTemplate, List<ContainerAttribute<T>>> linkedAttributeLinkingAttributesMap,
                                                                     Map<AttributeTemplate, List<ContainerAttribute<T>>> linkingAttributeLinkedAttributesMaps) {
        List<AttributeAndLinkAttributes<T>> result = new ArrayList<>();
        containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> {
            List<ContainerAttribute<T>> linkingAttributes = linkedAttributeLinkingAttributesMap.get(attributeTemplate);
            List<ContainerAttribute<T>> linkedAttributes = linkingAttributeLinkedAttributesMaps.get(attributeTemplate);
            if (linkingAttributes != null || linkedAttributes != null) {
                AttributeAndLinkAttributes<T> attributeAndLinkAttributes = new AttributeAndLinkAttributes<>(containerAttributeKeys, attributeTemplate);
                attributeAndLinkAttributes.setLinkingAttributes(linkingAttributes);
                attributeAndLinkAttributes.setLinkedAttributes(linkedAttributes);
                result.add(attributeAndLinkAttributes);
            }
        });

        return result;
    }

    private Map<AttributeTemplate, List<ContainerAttribute<T>>> fetchLinkedAttributeLinkingAttributesMap(ConverterTemplate converterTemplate) {
        Map<AttributeTemplate, List<ContainerAttribute<T>>> result = new HashMap<>();
        converterTemplate.getContainerTemplates().forEach(containerTemplate -> {
            containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> {
                Optional<AttributeTemplate> linkedAttributeTemplate = converterTemplate.fetchLinkedAttributeTemplate(attributeTemplate);
                if (linkedAttributeTemplate.isPresent()) {
                    List<ContainerAttribute<T>> containerAttributes = result.get(linkedAttributeTemplate.get());
                    if (containerAttributes == null) {
                        containerAttributes = new ArrayList<>();
                        result.put(linkedAttributeTemplate.get(), containerAttributes);
                    }
                    containerAttributes.add(new ContainerAttribute<>(containerAttributeKeys, containerTemplate, attributeTemplate));
                }
            });
        });
        return result;
    }

    private Map<AttributeTemplate, List<ContainerAttribute<T>>> fetchLinkingAttributeLinkedAttributesMap(ConverterTemplate converterTemplate, Map<AttributeTemplate, List<ContainerAttribute<T>>> linkedAttributeLinkingAttributesMap) {
        Map<AttributeTemplate, List<ContainerAttribute<T>>> result = new HashMap<>();
        Map<AttributeTemplate, ContainerTemplate> attributeContainerMap = new HashMap<>();
        converterTemplate.getContainerTemplates().forEach(containerTemplate -> {
            containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> attributeContainerMap.put(attributeTemplate, containerTemplate));
        });
        linkedAttributeLinkingAttributesMap.keySet().forEach(linkedAttribute -> {
            ContainerAttribute<T> linkedContainerAttribute = new ContainerAttribute<>(containerAttributeKeys, attributeContainerMap.get(linkedAttribute), linkedAttribute);
            linkedAttributeLinkingAttributesMap.get(linkedAttribute).forEach(linkingAttribute -> {
                List<ContainerAttribute<T>> linkedAttributes = result.get(linkingAttribute.getAttributeTemplate());
                if (linkedAttributes == null) {
                    linkedAttributes = new ArrayList<>();
                    result.put(linkingAttribute.getAttributeTemplate(), linkedAttributes);
                }
                linkedAttributes.add(linkedContainerAttribute);
            });
        });

        return result;
    }

}
