package de.samply.converter;

import de.samply.csv.ContainersToCsvConverter;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Disabled
class ConverterManagerTest {

  private final String OUTPUT_DIRECTORY = "./output";
  private final String sourceId = "blaze-store";
  private final String CONVERTER_APPLICATION_CONTEXT_PATH = "./converter/converter.xml";

  private BundleToContainersConverter bundleToContainersConverter = new BundleToContainersConverter();
  private ContainersToCsvConverter containersToCsvConverter = new ContainersToCsvConverter(OUTPUT_DIRECTORY);

  @Test
  void getConverter() {
    ConverterManager converterManager = new ConverterManager(bundleToContainersConverter, containersToCsvConverter, CONVERTER_APPLICATION_CONTEXT_PATH);
    Converter converter = converterManager.getBestMatchConverter(Format.FHIR_QUERY, Format.CSV, sourceId);
    ConverterTemplateManager converterTemplateManager = new ConverterTemplateManager("./templates");
    ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate("test-template1");
    Flux flux = converter.convert(Flux.just("Patient"), converterTemplate);
    flux.blockLast();
    //TODO
  }

}
