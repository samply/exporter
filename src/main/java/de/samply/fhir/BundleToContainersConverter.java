package de.samply.fhir;


import ca.uhn.fhir.context.FhirContext;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.container.Attribute;
import de.samply.container.Containers;
import de.samply.converter.ConverterImpl;
import de.samply.converter.Format;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ExpressionNode;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class BundleToContainersConverter extends
    ConverterImpl<Bundle, Containers, BundleToContainersConverterSession> {

  private final FHIRPathEngine fhirPathEngine;

  public BundleToContainersConverter() {
    this.fhirPathEngine = createFhirPathEngine();
  }

  private class BundleContext {

    private Map<String, Resource> idResourceMap;
    private BundleToContainersConverterSession session;
    private Table<String, String, String> idValueAnonymMap = HashBasedTable.create();

    public BundleContext(Bundle bundle, BundleToContainersConverterSession session) {
      this.idResourceMap = fetchIdResourceMap(bundle);
      this.session = session;
    }

    public String fetchAnonym(AttributeTemplate attributeTemplate, String value) {
      String anonym = idValueAnonymMap.get(attributeTemplate.getAnonym(), value);
      if (anonym == null) {
        anonym = session.generateAnonym(attributeTemplate);
        idValueAnonymMap.put(attributeTemplate.getAnonym(), value, anonym);
      }
      return anonym;
    }

    public Resource getResource(String resourceId) {
      return idResourceMap.get(resourceId);
    }

  }

  @Override
  public Flux<Containers> convert(Bundle bundle, ConverterTemplate converterTemplate,
      BundleToContainersConverterSession session) {
    return Flux.just(convertToContainers(bundle, converterTemplate, session));
  }

  @Override
  protected BundleToContainersConverterSession initializeSession() {
    return new BundleToContainersConverterSession();
  }

  public Containers convertToContainers(Bundle bundle, ConverterTemplate converterTemplate,
      BundleToContainersConverterSession session) {
    Containers containers = new Containers();
    BundleContext context = new BundleContext(bundle, session);
    if (converterTemplate != null) {
      converterTemplate.getContainerTemplates()
          .forEach(containerTemplate -> addContainers(bundle, containers, containerTemplate,
              context));
    }
    return containers;
  }

  private Map<String, Resource> fetchIdResourceMap(Bundle bundle) {
    Map<String, Resource> result = new HashMap<>();
    bundle.getEntry().forEach(bundleEntryComponent -> {
      Resource resource = bundleEntryComponent.getResource();
      String id = resource.getResourceType() + "/" + resource.getIdPart();
      result.put(id, resource);
    });
    return result;
  }

  private void addContainers(Bundle bundle, Containers containers,
      ContainerTemplate containerTemplate, BundleContext context) {
    containerTemplate.getAttributeTemplates().forEach(attributeTemplate ->
        bundle.getEntry().forEach(bundleEntryComponent -> {
          if (isSameResourceType(bundleEntryComponent.getResource(), attributeTemplate)) {
            fetchResourceAttribute(bundleEntryComponent.getResource(), containerTemplate,
                attributeTemplate, context).forEach(resourceAttribute ->
                addResourceAttributeToContainers(containers, resourceAttribute, context));
          }
        }));
  }

  private boolean isSameResourceType(Resource resource, AttributeTemplate attributeTemplate) {
    return (attributeTemplate.getParentFhirPath() != null &&
        isSameResourceType(resource, attributeTemplate.getParentFhirPath())) ||
        (attributeTemplate.getChildFhirPath() != null &&
            isSameResourceType(resource, attributeTemplate.getChildFhirPath())) ||
        isSameResourceType(resource, attributeTemplate.getFhirPath());
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
      ContainerTemplate containerTemplate, AttributeTemplate attributeTemplate,
      BundleContext context) {
    List<ResourceAttribute> resourceAttributes = new ArrayList<>();
    Resource relatedResource = fetchRelatedResource(resource, attributeTemplate, context);
    if (relatedResource != null) {
      ExpressionNode expressionNode = fhirPathEngine.parse(attributeTemplate.getFhirPath());
      Resource evalResource =
          (attributeTemplate.getChildFhirPath() != null) ? relatedResource : resource;
      Resource idResource =
          (attributeTemplate.getParentFhirPath() != null) ? relatedResource : resource;
      fhirPathEngine.evaluate(evalResource, expressionNode)
          .forEach(base -> resourceAttributes.add(
              new ResourceAttribute(idResource, base.toString(), containerTemplate,
                  attributeTemplate)));
    }
    return resourceAttributes;
  }

  private Resource fetchRelatedResource(Resource currentResource,
      AttributeTemplate attributeTemplate, BundleContext context) {
    AtomicReference<Resource> result = new AtomicReference<>(currentResource);
    if (attributeTemplate.getParentFhirPath() != null) {
      attributeTemplate.fetchParentFhirPaths().forEach(parentFhirPath -> {
        if (result.get() != null) {
          result.set(fetchRelatedResource(result.get(), parentFhirPath, context));
        }
      });
    }
    if (attributeTemplate.getChildFhirPath() != null) {
      attributeTemplate.fetchChildFhirPaths().forEach(childFhirPath -> {
        if (result.get() != null) {
          result.set(fetchRelatedResource(result.get(), childFhirPath, context));
        }
      });
    }
    return result.get();
  }

  private Resource fetchRelatedResource(Resource resource, String parentFhirPath,
      BundleContext context) {
    Resource result = null;
    ExpressionNode expressionNode = fhirPathEngine.parse(parentFhirPath);
    List<Base> resourceIds = fhirPathEngine.evaluate(resource, expressionNode);
    if (resourceIds.size() > 0) {
      String resourceId = resourceIds.get(0).toString();
      result = context.getResource(resourceId);
    }
    return result;
  }

  private void addResourceAttributeToContainers(Containers containers,
      ResourceAttribute resourceAttribute, BundleContext context) {
    containers.addAttribute(resourceAttribute.containerTemplate(),
        resourceAttribute.fetchContainerId(),
        new Attribute(resourceAttribute.attributeTemplate(),
            fetchResourceAttributeValue(resourceAttribute, context)));
  }

  private String fetchResourceAttributeValue(ResourceAttribute resourceAttribute,
      BundleContext context) {
    String result = resourceAttribute.attributeValue();
    if (resourceAttribute.attributeTemplate().getOperation() != null){
      result = resourceAttribute.attributeTemplate().getOperation().execute(result);
    }
    if (resourceAttribute.attributeTemplate().getAnonym() != null) {
      result = context.fetchAnonym(resourceAttribute.attributeTemplate(), result);
    }
    return result;
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
