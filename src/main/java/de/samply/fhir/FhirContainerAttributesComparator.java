package de.samply.fhir;

import de.samply.container.Attribute;
import de.samply.container.Container;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FhirContainerAttributesComparator implements ContainerAttributesComparator {

    private Map<ContainerTemplate, ContainerAttributesFhirDependencies> templatIdFhirDependencies = new HashMap<>();
    private final FHIRPathEngine fhirPathEngine;

    public FhirContainerAttributesComparator(FHIRPathEngine fhirPathEngine) {
        this.fhirPathEngine = fhirPathEngine;
    }

    @Override
    public boolean belongsAttributeToContainer(Attribute attribute, Container container) {
        List<AttributeTemplate> attributeDependencies = fetchContainerAttributesFhirDependencies(container)
                .getFhirDependencies(attribute.attributeTemplate());
        String mergedFhirPath = attribute.attributeTemplate().getValFhirPath();
        for (AttributeTemplate attributeDependency : attributeDependencies) {
            Optional<Attribute> tempAttribute = container.getAttribute(attributeDependency);
            if (tempAttribute.isPresent()) {
                // Both attribute and the dependency need to have the same id resource and the same value resource
                // TODO: Actually, it could happen that the dependency has the value in a different resource than the attribute, but currently we don't support it.
                // Please make the appropiate changes for that.
                if (tempAttribute.get().idResource() != attribute.idResource() || tempAttribute.get().valueResource() != attribute.valueResource()) {
                    return false;
                }
                mergedFhirPath = FhirPathMerger.merge(mergedFhirPath, tempAttribute.get().attributeTemplate().getValFhirPath(), tempAttribute.get().value());
            }
        }
        // If there are no dependencies, it is included
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
