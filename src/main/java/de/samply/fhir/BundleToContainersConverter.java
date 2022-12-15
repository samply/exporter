package de.samply.fhir;


import ca.uhn.fhir.context.FhirContext;
import de.samply.converter.ConverterImpl;
import de.samply.container.Attribute;
import de.samply.container.Container;
import de.samply.container.Containers;
import de.samply.converter.Format;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ExpressionNode;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class BundleToContainersConverter extends ConverterImpl<Bundle, Containers> {

  private final FHIRPathEngine fhirPathEngine;

  public BundleToContainersConverter() {
    this.fhirPathEngine = createFhirPathEngine();
  }

  @Override
  public Flux<Containers> convert(Bundle bundle, ConverterTemplate converterTemplate) {
    return Flux.just(convertToContainers(bundle, converterTemplate));
  }

  public Containers convertToContainers(Bundle bundle, ConverterTemplate converterTemplate) {
    Containers containers = new Containers();
    if (converterTemplate != null) {
      converterTemplate.getContainerTemplates()
          .forEach(containerTemplate -> addContainers(bundle, containers, containerTemplate));
    }
    return containers;
  }

  private void addContainers(Bundle bundle, Containers containers,
      ContainerTemplate containerTemplate) {
    containerTemplate.getAttributeTemplates().forEach(attributeTemplate ->
        bundle.getEntry().forEach(bundleEntryComponent -> {
          if (isSameResourceType(bundleEntryComponent.getResource(), attributeTemplate.getFhirPath())) {
            fetchResourceAttribute(bundleEntryComponent.getResource(), containerTemplate,
                attributeTemplate).forEach(resourceAttribute ->
                addResourceAttributeToContainers(containers, resourceAttribute));
          }
        }));
  }

  private boolean isSameResourceType(Resource resource, String fhirPath) {
    String resourceType = resource.getMeta().getProfile().get(0).getValue();
    if (resourceType.contains("/")) {
      resourceType = resourceType.substring(resourceType.lastIndexOf('/') + 1);
    }
    if (fhirPath.contains(".")) {
      fhirPath = fhirPath.substring(0, fhirPath.indexOf("."));
    }
    return resourceType.toLowerCase().contains(fhirPath.toLowerCase());
  }

  private List<ResourceAttribute> fetchResourceAttribute(Resource resource,
      ContainerTemplate containerTemplate, AttributeTemplate attributeTemplate) {
    List<ResourceAttribute> resourceAttributes = new ArrayList<>();
    ExpressionNode expressionNode = fhirPathEngine.parse(attributeTemplate.getFhirPath());
    fhirPathEngine.evaluate(resource, expressionNode)
        .forEach(base -> resourceAttributes.add(
            new ResourceAttribute(resource, base.toString(), containerTemplate,
                attributeTemplate)));
    return resourceAttributes;
  }

  private void addResourceAttributeToContainers(Containers containers,
      ResourceAttribute resourceAttribute) {
    Container container = containers.getContainer(resourceAttribute.containerTemplate(),
        resourceAttribute.fetchContainerId());
    if (container == null){
      container = new Container(resourceAttribute.fetchContainerId(), resourceAttribute.containerTemplate());
      containers.addContainer(container);
    }
    container.addAttribute(new Attribute(resourceAttribute.attributeTemplate(), resourceAttribute.attributeValue()));
  }

  private FHIRPathEngine createFhirPathEngine() {
    FhirContext fhirContext = FhirContext.forR4();
    return new FHIRPathEngine(
        new HapiWorkerContext(fhirContext, fhirContext.getValidationSupport()));
  }

  @Override
  public Format getInputFormat() {
    return Format.BUNDLE;
  }

  @Override
  public Format getOutputFormat() {
    return Format.CONTAINERS;
  }

}
