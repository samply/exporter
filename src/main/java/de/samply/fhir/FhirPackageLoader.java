package de.samply.fhir;

import de.samply.exporter.ExporterConst;
import java.io.IOException;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;

public class FhirPackageLoader {

  private final String fhirPackageDirectory;

  public FhirPackageLoader(String fhirPackageDirectory) {
    this.fhirPackageDirectory = fhirPackageDirectory;
  }

  public void loadPackage(NpmPackageValidationSupport npmPackageSupport,
      String fhirPackageUrl)
      throws IOException {
    npmPackageSupport.loadPackageFromClasspath(createFhirPackagePath(fhirPackageUrl));
  }

  private String createFhirPackagePath(String fhirPackage) {
    //TODO
    return createClasspath(fhirPackage);
  }

  private String createClasspath(String fhirPackage){
    return ExporterConst.FHIR_PACKAGE_ROOT_CLASSPATH + "/" + fhirPackage;
  }

}
