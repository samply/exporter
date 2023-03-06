package de.samply.converter;

import de.samply.EnvironmentTestUtils;
import de.samply.csv.ContainersToCsvConverter;
import de.samply.excel.ContainersToExcelConverter;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import de.samply.template.ConverterTemplateUtils;
import de.samply.utils.EnvironmentUtils;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;

@Disabled
class ConverterManagerTest {

  private final String OUTPUT_DIRECTORY = "./output";
  private final String sourceId = "blaze-store";
  private String targetId;
  private final String CONVERTER_APPLICATION_CONTEXT_PATH = "./converter/converter.xml";

  private BundleToContainersConverter bundleToContainersConverter = new BundleToContainersConverter();
  private ContainersToCsvConverter containersToCsvConverter;
  private ContainersToExcelConverter containersToExcelConverter;
  private ApplicationContext applicationContext;

  @BeforeEach
  void setUp() {
    EnvironmentUtils environmentUtils = new EnvironmentUtils(
        EnvironmentTestUtils.getEmptyMockEnvironment());
    ConverterTemplateUtils converterTemplateUtils = new ConverterTemplateUtils(environmentUtils);
    this.containersToCsvConverter = new ContainersToCsvConverter(converterTemplateUtils,
        OUTPUT_DIRECTORY);
    this.containersToExcelConverter = new ContainersToExcelConverter(30000000,
        converterTemplateUtils, OUTPUT_DIRECTORY);
  }

  @Test
  void getConverter() {
    ConverterManager converterManager = new ConverterManager(applicationContext,
        bundleToContainersConverter, containersToCsvConverter, containersToExcelConverter,
        CONVERTER_APPLICATION_CONTEXT_PATH);
    Converter converter = converterManager.getBestMatchConverter(Format.FHIR_QUERY, Format.CSV,
        sourceId, targetId);
    ConverterTemplateManager converterTemplateManager = new ConverterTemplateManager("./templates");

    Set<String> sourceIds = converterManager.getSourceIds();
    //TODO
    Set<String> converterTemplateIds = converterTemplateManager.getConverterTemplateIds();
    //TODO

    ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate(
        "test-template1");
    Flux flux = converter.convert(Flux.just("Patient"), converterTemplate);
    flux.blockLast();
    //TODO

  }

}
