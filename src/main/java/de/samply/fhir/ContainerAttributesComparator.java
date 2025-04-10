package de.samply.fhir;

import de.samply.container.Attribute;
import de.samply.container.Container;

public interface ContainerAttributesComparator {

    boolean belongsAttributeToContainer(Attribute attribute, Container container);

}
