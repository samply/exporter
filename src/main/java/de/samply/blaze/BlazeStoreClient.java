package de.samply.blaze;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.teiler.TeilerConst;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.ExpressionNode;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.hl7.fhir.r4.model.Bundle;

@Component
public class BlazeStoreClient {

  private final IGenericClient client;
  private final FHIRPathEngine fhirPathEngine;


  public BlazeStoreClient(@Value(TeilerConst.BLAZE_STORE_URL_SV) String blazeStoreUrl) {
    FhirContext fhirContext = FhirContext.forR4();
    this.client = createFhirClient(fhirContext, getBlazeStoreFhirApiUrl(blazeStoreUrl));
    this.fhirPathEngine = createFhirPathEngine(fhirContext);
  }

  public void retrieve() {
    String pseudonym = "ZGPQEA3R";
    Bundle bundle = fetchPatientBundle(pseudonym);
    doSomething(bundle);

  }

  private void doSomething(Bundle bundle){
    Resource patient = getFirstPatient(bundle);
    String attribute = fetchAttribute("Patient.gender.value", patient);
    Resource histologie = getFirstHistologie(bundle);
    String attribute2 = fetchAttribute("Observation.value.coding.code", histologie);
    String attribute3 = fetchAttribute("Observation.value.coding.code", patient);
    String attribute4 = fetchAttribute("Bundle.entry.select(resource as Patient).gender.value", bundle);
    String attribute5 = fetchAttribute("Bundle.entry.select(resource as Observation).where(code.coding.code = '59847-4').value.coding.code", bundle);
    String attribute6 = fetchAttribute("Bundle.entry.select(resource as Observation).subject.reference.value", bundle);
  }

  private String fetchAttribute(String fhirPath, Resource resource){
    ExpressionNode genderNode = fhirPathEngine.parse(fhirPath);
    List<Base> baseList = fhirPathEngine.evaluate(resource, genderNode);
    return (baseList != null && baseList.size() > 0) ? baseList.get(0).toString() : null;
  }

  private Resource getFirstPatient(Bundle bundle){
    for (BundleEntryComponent component : bundle.getEntry()){
      if (component.getResource() instanceof Patient){
          return component.getResource();
      }
    }
    return null;
  }

  private Resource getFirstHistologie(Bundle bundle){
    for (BundleEntryComponent component : bundle.getEntry()){
      if (component.getResource() instanceof Observation &&
          component.getResource().getMeta().getProfile().get(0).toString().contains("Histologie")){
        return component.getResource();
      }
    }
    return null;
  }

  private Bundle fetchPatientBundle(String patientPseudonym) {
    return client.search()
        .byUrl("Patient?identifier=" + patientPseudonym)
        .revInclude(new Include("Observation:patient"))
        .revInclude(new Include("Condition:patient"))
        .revInclude(new Include("Specimen:patient"))
        .revInclude(new Include("Procedure:patient"))
        .revInclude(new Include("MedicationStatement:patient"))
        .revInclude(new Include("ClinicalImpression:patient"))
        .returnBundle(Bundle.class)
        .execute();
  }

  private String getBlazeStoreFhirApiUrl(String blazeStoreUrl) {
    try {
      return new URL(new URL(blazeStoreUrl), TeilerConst.BLAZE_STORE_API_PATH).toString();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private IGenericClient createFhirClient(FhirContext fhirContext, String blazeStoreUrl) {
    // TODO: set proxy
    return fhirContext.newRestfulGenericClient(blazeStoreUrl);
  }

  private FHIRPathEngine createFhirPathEngine(FhirContext fhirContext) {
    return new FHIRPathEngine(new HapiWorkerContext(fhirContext, fhirContext.getValidationSupport()));
  }


}
