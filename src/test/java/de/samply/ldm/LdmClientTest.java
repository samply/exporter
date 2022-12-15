package de.samply.ldm;

import static org.junit.jupiter.api.Assertions.*;

import de.samply.converter.ConverterManager;
import de.samply.converter.Format;
import de.samply.csv.ContainersToCsvConverter;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.query.QueryManager;
import de.samply.teiler.TeilerConst;
import de.samply.template.ConverterTemplateManager;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    BundleToContainersConverter bundleToContainersConverter = new BundleToContainersConverter();
    ContainersToCsvConverter containersToCsvConverter = new ContainersToCsvConverter(writeDirectory);
    ConverterManager converterManager = new ConverterManager(bundleToContainersConverter, containersToCsvConverter, converterXmlApplicationContextPath);
    ConverterTemplateManager converterTemplateManager = new ConverterTemplateManager(templateDirectory);
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
