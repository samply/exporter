package de.samply.core;

import de.samply.EnvironmentTestUtils;
import de.samply.converter.ConverterManager;
import de.samply.converter.Format;
import de.samply.csv.ContainersToCsvConverter;
import de.samply.db.repository.InquiryRespository;
import de.samply.db.repository.QueryExecutionErrorRepository;
import de.samply.db.repository.QueryExecutionFileRepository;
import de.samply.db.repository.QueryExecutionRepository;
import de.samply.db.repository.QueryRepository;
import de.samply.excel.ContainersToExcelConverter;
import de.samply.exporter.ExporterConst;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.db.crud.ExporterDbService;
import de.samply.json.ContainersToJsonConverter;
import de.samply.template.ConverterTemplateManager;
import de.samply.template.ConverterTemplateUtils;
import de.samply.utils.EnvironmentUtils;
import de.samply.xml.ContainersToXmlConverter;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;

@Disabled
class ExporterCoreTest {

  private final String converterXmlApplicationContextPath = "./converter/converter.xml";
  private final String templateDirectory = "./templates";
  private final String writeDirectory = "./output";
  private String fhirPackagesDirectory = "./fhir-packages";
  private final String converterTemplateId = "test-template1";
  private ExporterCore exporterCore;

  @BeforeEach
  void setUp() {
    EnvironmentUtils environmentUtils = new EnvironmentUtils(
        EnvironmentTestUtils.getEmptyMockEnvironment());
    ConverterTemplateUtils converterTemplateUtils = new ConverterTemplateUtils(
        ExporterConst.DEFAULT_TIMESTAMP_FORMAT, environmentUtils);
    ContainersToCsvConverter containersToCsvConverter = new ContainersToCsvConverter(
        converterTemplateUtils, writeDirectory);
    ContainersToExcelConverter containersToExcelConverter = new ContainersToExcelConverter(30000000,
        converterTemplateUtils, writeDirectory);
    ContainersToJsonConverter containersToJsonConverter = new ContainersToJsonConverter(
        converterTemplateUtils, writeDirectory);
    ContainersToXmlConverter containersToXmlConverter = new ContainersToXmlConverter(
        converterTemplateUtils, writeDirectory);
    BundleToContainersConverter bundleToContainersConverter = new BundleToContainersConverter(
        fhirPackagesDirectory, false);
    ApplicationContext applicationContext = null;
    ConverterManager converterManager = new ConverterManager(applicationContext,
        bundleToContainersConverter, containersToCsvConverter, containersToExcelConverter,
        containersToJsonConverter, containersToXmlConverter, converterXmlApplicationContextPath);
    ConverterTemplateManager converterTemplateManager = new ConverterTemplateManager(
        templateDirectory);
    //TODO
    QueryRepository queryRepository = null;
    QueryExecutionRepository queryExecutionRepository = null;
    QueryExecutionFileRepository queryExecutionFileRepository = null;
    QueryExecutionErrorRepository queryExecutionErrorRepository = null;
    InquiryRespository inquiryRespository = null;
    ExporterDbService exporterDbService = new ExporterDbService(queryRepository,
        queryExecutionRepository,
        queryExecutionFileRepository, queryExecutionErrorRepository, inquiryRespository);

    exporterCore = new ExporterCore(converterManager, converterTemplateManager, exporterDbService);
  }

  @Test
  void retrieveByQueryId() throws ExporterCoreException {
    ExporterParameters exporterParameters = new ExporterParameters(null, "Patient",
        converterTemplateId, null, null, Format.FHIR_QUERY, null, null, null, null, Format.CSV);
    ExporterCoreParameters exporterCoreParameters = exporterCore.extractParameters(
        exporterParameters);
    Flux<Path> resultFlux = exporterCore.retrieveQuery(exporterCoreParameters);
    resultFlux.blockLast();
  }

  @Test
  void retrieveByQuery() {
  }

}
