package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.template.AttributeTemplate;
import de.samply.template.ConverterTemplate;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

public class BundleContext {


  private ResourceFinder resourceFinder;
  private BundleValidator bundleValidator;
  private BundleToContainersConverterSession session;
  private Table<String, String, String> idValueAnonymMap = HashBasedTable.create();


  public BundleContext(Bundle bundle, BundleToContainersConverterSession session,
      FHIRPathEngine fhirPathEngine, FhirContext fhirContext, ConverterTemplate converterTemplate,
      String fhirPackagesDirectory, boolean logFhirValidation)
      throws BundleContextException {
    this.resourceFinder = new ResourceFinder(bundle, fhirPathEngine);
    this.session = session;
    this.bundleValidator = createBundleValidator(fhirContext, converterTemplate, fhirPackagesDirectory, logFhirValidation);
  }

  private BundleValidator createBundleValidator(FhirContext fhirContext,
      ConverterTemplate converterTemplate, String fhirPackageDirectory, boolean logFhirValidation)
      throws BundleContextException {
    try {
      return new BundleValidator(fhirContext, converterTemplate, fhirPackageDirectory, logFhirValidation);
    } catch (BundleValidatorException e) {
      throw new BundleContextException(e);
    }
  }

  public String fetchAnonym(AttributeTemplate attributeTemplate, String value) {
    String anonym = idValueAnonymMap.get(attributeTemplate.getAnonym(), value);
    if (anonym == null) {
      anonym = session.generateAnonym(attributeTemplate);
      idValueAnonymMap.put(attributeTemplate.getAnonym(), value, anonym);
    }
    return anonym;
  }

  public List<Resource> fetchRelatedResources(Resource currentResource,
      AttributeTemplate attributeTemplate) {
    return resourceFinder.fetchRelatedResources(currentResource, attributeTemplate);
  }

  public String validate(Resource resource, AttributeTemplate attributeTemplate) {
    return bundleValidator.validate(resource, attributeTemplate);
  }

}
