package de.samply.template.graph.factory;

import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.graph.ContainerAttributeKeys;

public class JsonContainerAttributeKeys implements ContainerAttributeKeys {

    @Override
    public String getContainerKey(ContainerTemplate containerTemplate) {
        return containerTemplate.getJsonKey();
    }

    @Override
    public String getAttributeKey(AttributeTemplate attributeTemplate) {
        return attributeTemplate.getJsonKey();
    }

}
