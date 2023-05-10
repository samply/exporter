package de.samply.fhir;

import java.io.IOException;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;

public class FhirPackageLoader {

  public static void loadPackage(NpmPackageValidationSupport npmPackageSupport,
      String fhirPackageUrl)
      throws IOException {
    npmPackageSupport.loadPackageFromClasspath(createFhirPackagePath(fhirPackageUrl));
  }

  private static String createFhirPackagePath(String fhirPackage) {
    return "fhir-packages/" + fhirPackage;
  }

}
