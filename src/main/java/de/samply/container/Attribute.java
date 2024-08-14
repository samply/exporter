package de.samply.container;

import de.samply.template.AttributeTemplate;
import org.hl7.fhir.r4.model.Resource;

public record Attribute(
        // If isContainerRef is true, then type is the container-type.
        AttributeTemplate attributeTemplate,
        String value,
        Resource idResource,
        Resource valueResource) {

}
