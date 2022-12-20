package de.samply.fhir;

import de.samply.container.Containers;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import de.samply.csv.ContainersToCsvConverter;
import java.nio.file.Path;
import java.util.HashMap;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Disabled
class FhirQueryToBundleConverterTest {

  private FhirQueryToBundleConverter fhirQueryToBundleConverter;
  private ConverterTemplateManager converterTemplateManager;
  private BundleToContainersConverter bundleToContainersConverter;
  private ContainersToCsvConverter containersToCsvConverter;

  private String blazeStoreUrl = "http://localhost:8091/fhir";
  private String sourceId = "blazeStore";
  private String templateDirectory = "./templates";
  private String outputDirectory = "./output";
  private String templateId = "test-template1";

  @BeforeEach
  void setUp() {
    this.fhirQueryToBundleConverter = new FhirQueryToBundleConverter(blazeStoreUrl, sourceId);
    this.containersToCsvConverter = new ContainersToCsvConverter(outputDirectory);
    this.converterTemplateManager = new ConverterTemplateManager(templateDirectory);
    this.bundleToContainersConverter = new BundleToContainersConverter();
  }

  @Test
  void testExecute() {
    ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate(templateId);
    fhirQueryToBundleConverter.convert(Flux.just("Patient"), converterTemplate).subscribe(bundle -> {
      Containers containers = bundleToContainersConverter.convertToContainers(bundle,
          converterTemplate);
      containersToCsvConverter.writeContainersInCsv(containers, converterTemplate, new HashMap<>());
    });
    //TODO
  }

  @Test
  void testConverters(){
    ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate(templateId);
    Flux<String> stringFlux = Flux.just("Patient");
    Flux<Bundle> bundleFlux = fhirQueryToBundleConverter.convert(stringFlux, converterTemplate);
    Flux<Containers> containersFlux = bundleToContainersConverter.convert(bundleFlux,
        converterTemplate);
    Flux<Path> pathFlux = containersToCsvConverter.convert(containersFlux, converterTemplate);
    pathFlux.blockLast();
    //TODO
  }

}
