package de.samply.ldm;

import de.samply.EnvironmentTestUtils;
import de.samply.converter.ConverterManager;
import de.samply.converter.Format;
import de.samply.csv.ContainersToCsvConverter;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.query.QueryManager;
import de.samply.template.ConverterTemplateManager;
import de.samply.template.ConverterTemplateUtils;
import de.samply.utils.EnvironmentUtils;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Disabled
class LdmClientTest {

  private final String converterXmlApplicationContextPath = "./converter/converter.xml";
  private final String templateDirectory = "./templates";
  private final String writeDirectory = "./output";
  private final String sourceId = "blaze-store";
  private final String converterTemplateId = "test-template1";
  private LdmClient ldmClient;

  @BeforeEach
  void setUp() {
    EnvironmentUtils environmentUtils = new EnvironmentUtils(
        EnvironmentTestUtils.getEmptyMockEnvironment());
    ConverterTemplateUtils converterTemplateUtils = new ConverterTemplateUtils(environmentUtils);
    ContainersToCsvConverter containersToCsvConverter = new ContainersToCsvConverter(
        converterTemplateUtils,
        writeDirectory);
    BundleToContainersConverter bundleToContainersConverter = new BundleToContainersConverter();
    ConverterManager converterManager = new ConverterManager(bundleToContainersConverter,
        containersToCsvConverter, converterXmlApplicationContextPath);
    ConverterTemplateManager converterTemplateManager = new ConverterTemplateManager(
        templateDirectory);
    QueryManager queryManager = new QueryManager();

    ldmClient = new LdmClient(converterManager, converterTemplateManager, queryManager);
  }

  @Test
  void retrieveByQueryId() throws LdmClientException {
    Flux<Path> resultFlux = ldmClient.retrieveByQuery(sourceId, "Patient", Format.FHIR_QUERY,
        Format.CSV, converterTemplateId);
    resultFlux.blockLast();
  }

  @Test
  void retrieveByQuery() {
  }

}
