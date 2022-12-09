package de.samply.blaze;

import de.samply.container.Containers;
import de.samply.container.template.ResultTemplates;
import de.samply.container.write.ContainersCsvWriter;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class FhirBundleToContainersConverterTest {

  private String outputDirectory = "./output";
  private String templateDirectory = "./templates";
  private String blazeStoreUrl = "http://localhost:8091";
  private String templateId = "test-template1";
  private String query;
  private String queryFormat;
  private FhirBundleToContainersConverter bundleToContainersConverter;
  private BlazeStoreClient blazeStoreClient;
  private ContainersCsvWriter containersCsvWriter;

  @BeforeEach
  void setUp() {
    ResultTemplates resultTemplates = new ResultTemplates(templateDirectory);
    this.bundleToContainersConverter = new FhirBundleToContainersConverter(resultTemplates);
    this.blazeStoreClient = new BlazeStoreClient(blazeStoreUrl);
    this.containersCsvWriter = new ContainersCsvWriter(outputDirectory);
  }

  @Test
  void testConvert() {
    Bundle bundle = blazeStoreClient.retrieve(query, queryFormat);
    Containers containers = bundleToContainersConverter.convert(bundle, templateId);
    containersCsvWriter.writeContainersInCsv(containers);
  }

}
