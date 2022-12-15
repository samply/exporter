package de.samply.fhir;

import de.samply.container.Containers;
import de.samply.template.conversion.ConversionTemplate;
import de.samply.template.conversion.ConversionTemplateManager;
import de.samply.csv.ContainersToCsvConverter;
import java.nio.file.Path;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Disabled
class FhirQueryToBundleConverterTest {

  private FhirQueryToBundleConverter fhirQueryToBundleConverter;
  private ConversionTemplateManager conversionTemplateManager;
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
    this.conversionTemplateManager = new ConversionTemplateManager(templateDirectory);
    this.bundleToContainersConverter = new BundleToContainersConverter();
  }

  @Test
  void testExecute() {
    ConversionTemplate conversionTemplate = conversionTemplateManager.getConversionTemplate(templateId);
    fhirQueryToBundleConverter.convert(Flux.just("Patient"), conversionTemplate).subscribe(bundle -> {
      Containers containers = bundleToContainersConverter.convertToContainers(bundle,
          conversionTemplate);
      containersToCsvConverter.writeContainersInCsv(containers, conversionTemplate);
    });
    //TODO
  }

  @Test
  void testConverters(){
    ConversionTemplate conversionTemplate = conversionTemplateManager.getConversionTemplate(templateId);
    Flux<String> stringFlux = Flux.just("Patient");
    Flux<Bundle> bundleFlux = fhirQueryToBundleConverter.convert(stringFlux, conversionTemplate);
    Flux<Containers> containersFlux = bundleToContainersConverter.convert(bundleFlux,
        conversionTemplate);
    Flux<Path> pathFlux = containersToCsvConverter.convert(containersFlux, conversionTemplate);
    pathFlux.blockLast();
    //TODO
  }

}
