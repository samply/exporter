package de.samply.template.graph;

import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;

public interface ContainerAttributeKeys {
    String getContainerKey(ContainerTemplate containerTemplate);
    String getAttributeKey(AttributeTemplate attributeTemplate);

}
