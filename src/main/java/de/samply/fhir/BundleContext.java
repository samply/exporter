package de.samply.fhir;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.template.AttributeTemplate;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public class BundleContext {

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

  private Map<String, Resource> fetchIdResourceMap(Bundle bundle) {
    Map<String, Resource> result = new HashMap<>();
    bundle.getEntry().forEach(bundleEntryComponent -> {
      Resource resource = bundleEntryComponent.getResource();
      String id = resource.getResourceType() + "/" + resource.getIdPart();
      result.put(id, resource);
    });
    return result;
  }

}
