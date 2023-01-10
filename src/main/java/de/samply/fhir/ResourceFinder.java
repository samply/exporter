package de.samply.fhir;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.teiler.TeilerConst;
import de.samply.template.AttributeTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ExpressionNode;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

public class ResourceFinder {

  private FHIRPathEngine fhirPathEngine;
  private Map<String, Resource> idResourceMap;
  private Table<String, Resource, List<Resource>> joinFhirPathParentResourceChildResources = HashBasedTable.create();

  public ResourceFinder(Bundle bundle, FHIRPathEngine fhirPathEngine) {
    this.fhirPathEngine = fhirPathEngine;
    this.idResourceMap = fetchIdResourceMap(bundle);
  }

  public Resource getResource(String resourceId) {
    return idResourceMap.get(resourceId);
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

  public List<Resource> fetchRelatedResources(Resource currentResource,
      AttributeTemplate attributeTemplate) {
    AtomicReference<List<Resource>> results = new AtomicReference<>(List.of(currentResource));
    if (attributeTemplate.getJoinFhirPath() != null) {
      attributeTemplate.fetchJoinFhirPaths().forEach(joinFhirPath -> {
        AtomicReference<List<Resource>> tempResults = new AtomicReference<>(new ArrayList<>());
        results.get().forEach(resource -> {
          if (AttributeTemplate.isChildFhirPath(joinFhirPath)
              && !attributeTemplate.isDirectJoinFhirPath()) {
            tempResults.get()
                .addAll(fetchChildRelatedResources(resource, joinFhirPath));
          } else {
            Resource tempResult = fetchParentRelatedResource(resource, joinFhirPath);
            if (tempResult != null) {
              tempResults.get().add(tempResult);
            }
          }
        });
        results.set(tempResults.get());
      });
    }
    return results.get();
  }

  private Resource fetchParentRelatedResource(Resource resource, String joinFhirPath) {
    joinFhirPath = AttributeTemplate.removeChildFhirPathHead(joinFhirPath);
    Resource result = null;
    ExpressionNode expressionNode = fhirPathEngine.parse(joinFhirPath);
    List<Base> resourceIds = fhirPathEngine.evaluate(resource, expressionNode);
    if (resourceIds.size() > 0) {
      String resourceId = resourceIds.get(0).toString();
      result = getResource(resourceId);
    }
    return result;
  }

  private List<Resource> fetchChildRelatedResources(Resource resource, String joinFhirPath) {
    final String joinFhirPath2 = AttributeTemplate.removeChildFhirPathHead(joinFhirPath);
    List<Resource> results = joinFhirPathParentResourceChildResources.get(joinFhirPath2, resource);
    if (results == null) {
      AtomicReference<List<Resource>> resultsReference = new AtomicReference<>(new ArrayList<>());
      results = resultsReference.get();
      joinFhirPathParentResourceChildResources.put(joinFhirPath2, resource, results);
      idResourceMap.values().forEach(tempResource -> {
        Resource parentRelatedResource = fetchParentRelatedResource(tempResource, joinFhirPath2);
        if (parentRelatedResource == resource) {
          resultsReference.get().add(tempResource);
        }
      });
    }
    return results;
  }


}
