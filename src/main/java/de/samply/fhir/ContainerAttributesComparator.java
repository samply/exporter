package de.samply.fhir;

import de.samply.container.Attribute;
import de.samply.container.Container;
import de.samply.template.AttributeTemplate;

import java.util.List;

public interface ContainerAttributesComparator {

    List<List<AttributeTemplate>> fetchIncompatibleCurrentAttributesOfContainerWithNewAttributes(Attribute attribute, Container container);

}
