package de.samply.fhir;

import de.samply.container.Attribute;
import de.samply.container.Container;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

import java.util.*;

public class FhirContainerAttributesComparator implements ContainerAttributesComparator {

    private Map<ContainerTemplate, ContainerAttributesFhirDependencies> templatIdFhirDependencies = new HashMap<>();
    private final FHIRPathEngine fhirPathEngine;

    public FhirContainerAttributesComparator(FHIRPathEngine fhirPathEngine) {
        this.fhirPathEngine = fhirPathEngine;
    }

    @Override
    public List<List<AttributeTemplate>> fetchIncompatibleCurrentAttributesOfContainerWithNewAttributes(Attribute attribute, Container container) {
        List<List<AttributeTemplate>> result = new ArrayList<>();
        List<AttributeTemplate> attributeDependencies = fetchContainerAttributesFhirDependencies(container)
                .getFhirDependencies(attribute.attributeTemplate());
        if (!areCompatibles(attribute, container, attributeDependencies)) {
            // TODO: Current simplification: If it is not compatible, we assume that the new attribute is not compatible with any of the dependencies.
            // This is not accurate, as it can be compatible with smaller combinations of dependencies.
            result.add(List.copyOf(attributeDependencies));
        }
        return result;
    }

    private boolean areCompatibles(Attribute attribute, Container container, List<AttributeTemplate> attributeDependencies) {
        String mergedFhirPath = attribute.attributeTemplate().getValFhirPath();
        for (AttributeTemplate attributeDependency : attributeDependencies) {
            Optional<Attribute> tempAttribute = container.getAttribute(attributeDependency);
            if (tempAttribute.isPresent()) {
                // Both attribute and the dependency need to have the same id resource and the same value resource
                // TODO: Actually, it could happen that the dependency has the value in a different resource than the attribute, but currently we don't support it.
                // Please make the appropiate changes for that.
                if (tempAttribute.get().idResource() != attribute.idResource() || tempAttribute.get().valueResource() != attribute.valueResource()) {
                    return false;
                } else {
                    mergedFhirPath = FhirPathMerger.merge(mergedFhirPath, tempAttribute.get().attributeTemplate().getValFhirPath(), tempAttribute.get().value());
                }
            }
        }
        return (attributeDependencies.size() > 0) ? isValueIncludedInTheResultsOfFhirPathExecution(attribute.value(), mergedFhirPath, attribute.valueResource()) : true;
    }

    private ContainerAttributesFhirDependencies fetchContainerAttributesFhirDependencies(Container container) {
        ContainerAttributesFhirDependencies containerAttributesFhirDependencies = templatIdFhirDependencies.get(container.getContainerTemplate());
        if (containerAttributesFhirDependencies == null) {
            containerAttributesFhirDependencies = new ContainerAttributesFhirDependencies(container.getContainerTemplate());
            templatIdFhirDependencies.put(container.getContainerTemplate(), containerAttributesFhirDependencies);
        }
        return containerAttributesFhirDependencies;
    }

    private boolean isValueIncludedInTheResultsOfFhirPathExecution(String value, String fhirPath, Resource resource) {
        return fhirPathEngine.evaluate(resource, fhirPathEngine.parse(fhirPath)).stream().map(Base::toString).toList().contains(value);
    }

}
