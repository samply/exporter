package de.samply.fhir;

import de.samply.container.Attribute;
import de.samply.container.Container;
import de.samply.template.AttributeTemplate;

import java.util.ArrayList;
import java.util.List;

public class BasicContainerAttributesComparator implements ContainerAttributesComparator {
    @Override
    public List<List<AttributeTemplate>> fetchIncompatibleCurrentAttributesOfContainerWithNewAttributes(Attribute attribute, Container container) {
        return new ArrayList<>();
    }
}
