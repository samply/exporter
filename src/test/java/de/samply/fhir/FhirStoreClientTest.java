package de.samply.fhir;

import de.samply.result.container.Containers;
import de.samply.result.container.template.ContainersTemplate;
import de.samply.result.container.template.ResultTemplatesManager;
import de.samply.result.container.write.ContainersCsvWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class FhirStoreClientTest {

  private FhirStoreClient fhirStoreClient;
  private ResultTemplatesManager resultTemplatesManager;
  private BundleToContainersConverter bundleToContainersConverter;
  private ContainersCsvWriter containersCsvWriter;

  private String blazeStoreUrl = "http://localhost:8091/fhir";
  private String templateDirectory = "./templates";
  private String outputDirectory = "./output";
  private String templateId = "test-template1";

  @BeforeEach
  void setUp() {
    this.fhirStoreClient = new FhirStoreClient(blazeStoreUrl);
    this.containersCsvWriter = new ContainersCsvWriter(outputDirectory);
    this.resultTemplatesManager = new ResultTemplatesManager(templateDirectory);
    this.bundleToContainersConverter = new BundleToContainersConverter();
  }

  @Test
  void testExecute() {
    ContainersTemplate resultTemplate = resultTemplatesManager.getResultTemplate(templateId);
    fhirStoreClient.execute("Patient", resultTemplate).subscribe(bundle -> {
      Containers containers = bundleToContainersConverter.convert(bundle, resultTemplate);
      containersCsvWriter.writeContainersInCsv(containers);
    });
    //TODO
  }

}
