package de.samply.fhir;

import de.samply.result.container.Containers;
import de.samply.result.container.template.ResultTemplate;
import de.samply.result.container.template.ResultTemplateManager;
import de.samply.result.container.write.ContainersCsvWriter;
import java.nio.file.Path;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Disabled
class FhirStoreClientTest {

  private FhirStoreClient fhirStoreClient;
  private ResultTemplateManager resultTemplateManager;
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
    this.resultTemplateManager = new ResultTemplateManager(templateDirectory);
    this.bundleToContainersConverter = new BundleToContainersConverter();
  }

  @Test
  void testExecute() {
    ResultTemplate resultTemplate = resultTemplateManager.getResultTemplate(templateId);
    fhirStoreClient.convert(Flux.just("Patient"), resultTemplate).subscribe(bundle -> {
      Containers containers = bundleToContainersConverter.convertToContainers(bundle,
          resultTemplate);
      containersCsvWriter.writeContainersInCsv(containers, resultTemplate);
    });
    //TODO
  }

  @Test
  void testConverters(){
    ResultTemplate resultTemplate = resultTemplateManager.getResultTemplate(templateId);
    Flux<String> stringFlux = Flux.just("Patient");
    Flux<Bundle> bundleFlux = fhirStoreClient.convert(stringFlux, resultTemplate);
    Flux<Containers> containersFlux = bundleToContainersConverter.convert(bundleFlux, resultTemplate);
    Flux<Path> pathFlux = containersCsvWriter.convert(containersFlux, resultTemplate);
    pathFlux.blockLast();
    //TODO
  }

}
