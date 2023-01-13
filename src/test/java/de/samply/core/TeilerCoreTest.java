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
import de.samply.fhir.BundleToContainersConverter;
import de.samply.db.crud.TeilerDbService;
import de.samply.template.ConverterTemplateManager;
import de.samply.template.ConverterTemplateUtils;
import de.samply.utils.EnvironmentUtils;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Disabled
class TeilerCoreTest {

  private final String converterXmlApplicationContextPath = "./converter/converter.xml";
  private final String templateDirectory = "./templates";
  private final String writeDirectory = "./output";
  private final String converterTemplateId = "test-template1";
  private TeilerCore teilerCore;

  @BeforeEach
  void setUp() {
    EnvironmentUtils environmentUtils = new EnvironmentUtils(
        EnvironmentTestUtils.getEmptyMockEnvironment());
    ConverterTemplateUtils converterTemplateUtils = new ConverterTemplateUtils(environmentUtils);
    ContainersToCsvConverter containersToCsvConverter = new ContainersToCsvConverter(
        converterTemplateUtils, writeDirectory);
    ContainersToExcelConverter containersToExcelConverter = new ContainersToExcelConverter(30000000,
        converterTemplateUtils, writeDirectory);
    BundleToContainersConverter bundleToContainersConverter = new BundleToContainersConverter();
    ConverterManager converterManager = new ConverterManager(bundleToContainersConverter,
        containersToCsvConverter, containersToExcelConverter, converterXmlApplicationContextPath);
    ConverterTemplateManager converterTemplateManager = new ConverterTemplateManager(
        templateDirectory);
    //TODO
    QueryRepository queryRepository = null;
    QueryExecutionRepository queryExecutionRepository = null;
    QueryExecutionFileRepository queryExecutionFileRepository = null;
    QueryExecutionErrorRepository queryExecutionErrorRepository = null;
    InquiryRespository inquiryRespository = null;
    TeilerDbService teilerDbService = new TeilerDbService(queryRepository, queryExecutionRepository,
        queryExecutionFileRepository, queryExecutionErrorRepository, inquiryRespository);

    teilerCore = new TeilerCore(converterManager, converterTemplateManager, teilerDbService);
  }

  @Test
  void retrieveByQueryId() throws TeilerCoreException {
    TeilerParameters teilerParameters = new TeilerParameters(null, "Patient",
        converterTemplateId, null, null, Format.FHIR_QUERY, null, null, null, null, Format.CSV);
    TeilerCoreParameters teilerCoreParameters = teilerCore.extractParameters(teilerParameters);
    Flux<Path> resultFlux = teilerCore.retrieveQuery(teilerCoreParameters);
    resultFlux.blockLast();
  }

  @Test
  void retrieveByQuery() {
  }

}
