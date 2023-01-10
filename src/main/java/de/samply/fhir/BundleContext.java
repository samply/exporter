package de.samply.fhir;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.template.AttributeTemplate;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

public class BundleContext {


  private ResourceFinder resourceFinder;
  private BundleToContainersConverterSession session;
  private Table<String, String, String> idValueAnonymMap = HashBasedTable.create();

  public BundleContext(Bundle bundle, BundleToContainersConverterSession session,
      FHIRPathEngine fhirPathEngine) {
    this.resourceFinder = new ResourceFinder(bundle, fhirPathEngine);
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

  public List<Resource> fetchRelatedResources(Resource currentResource, AttributeTemplate attributeTemplate){
    return resourceFinder.fetchRelatedResources(currentResource, attributeTemplate);
  }


}
