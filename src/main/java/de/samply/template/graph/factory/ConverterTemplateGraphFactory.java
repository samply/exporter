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

    public ConverterTemplateGraph<T> create(ConverterTemplate converterTemplate) {
        return new ConverterTemplateGraph<>(createContainerTemplateGraphs(converterTemplate));
    }

    private List<ContainerTemplateGraph<T>> createContainerTemplateGraphs(ConverterTemplate converterTemplate) {
        List<ContainerTemplateGraph<T>> result = new ArrayList<>();
        Map<AttributeTemplate, List<ContainerTemplateAttributeTemplate<T>>> linkedAttributeLinkingAttributesMap = fetchLinkedAttributeLinkingAttributesMap(converterTemplate);
        converterTemplate.getContainerTemplates().forEach(containerTemplate -> {
            result.add(new ContainerTemplateGraph<>(containerAttributeKeys, containerTemplate, createAttributeTemplateLinks(containerTemplate, linkedAttributeLinkingAttributesMap)));
        });
        return result;
    }

    private List<AttributeTemplateLinks<T>> createAttributeTemplateLinks(ContainerTemplate containerTemplate, Map<AttributeTemplate, List<ContainerTemplateAttributeTemplate<T>>> linkedAttributeLinkingAttributesMap) {
        List<AttributeTemplateLinks<T>> result = new ArrayList<>();
        containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> {
            List<ContainerTemplateAttributeTemplate<T>> linkingAttributes = linkedAttributeLinkingAttributesMap.get(attributeTemplate);
            if (linkingAttributes != null){
                result.add(new AttributeTemplateLinks<>(containerAttributeKeys, attributeTemplate, linkingAttributes));
            }
        });

        return result;
    }

    private Map<AttributeTemplate, List<ContainerTemplateAttributeTemplate<T>>> fetchLinkedAttributeLinkingAttributesMap(ConverterTemplate converterTemplate) {
        Map<AttributeTemplate, List<ContainerTemplateAttributeTemplate<T>>> result = new HashMap<>();
        converterTemplate.getContainerTemplates().forEach(containerTemplate -> {
            containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> {
                Optional<AttributeTemplate> linkedAttributeTemplate = converterTemplate.fetchLinkedAttributeTemplate(attributeTemplate);
                if (linkedAttributeTemplate.isPresent()) {
                    List<ContainerTemplateAttributeTemplate<T>> containerTemplateAttributeTemplates = result.get(linkedAttributeTemplate.get());
                    if (containerTemplateAttributeTemplates == null) {
                        containerTemplateAttributeTemplates = new ArrayList<>();
                        result.put(linkedAttributeTemplate.get(), containerTemplateAttributeTemplates);
                    }
                    containerTemplateAttributeTemplates.add(new ContainerTemplateAttributeTemplate<>(containerAttributeKeys, containerTemplate, attributeTemplate));
                }
            });
        });
        return result;
    }

}
