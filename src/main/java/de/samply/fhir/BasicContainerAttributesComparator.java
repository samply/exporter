package de.samply.fhir;

import de.samply.container.Attribute;
import de.samply.container.Container;

public class BasicContainerAttributesComparator implements ContainerAttributesComparator {
    @Override
    public boolean belongsAttributeToContainer(Attribute attribute, Container container) {
        return true;
    }
}
