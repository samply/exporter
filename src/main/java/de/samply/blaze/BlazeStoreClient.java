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

  public Bundle retrieve(String query, String queryFormat) {
    String pseudonym = "ZGPQEA3R";
    return fetchPatientBundle(pseudonym);
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
