package de.samply.core;

import de.samply.converter.ConverterManager;
import de.samply.converter.ConvertersTestConfiguration;
import de.samply.converter.Format;
import de.samply.db.crud.ExporterDbService;
import de.samply.db.repository.*;
import de.samply.template.ConverterTemplateManager;
import de.samply.template.token.TokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;

import java.nio.file.Path;

@Disabled
@SpringBootTest
@Import(ConvertersTestConfiguration.class)
class ExporterCoreTest {

    @Autowired
    private ApplicationContext applicationContext;
    private final String converterXmlApplicationContextPath = "./converter/converter.xml";
    private final String templateDirectory = "./templates";
    private final String converterTemplateId = "test-template1";
    private ExporterCore exporterCore;
    private TokenContext tokenContext = new TokenContext(null);

    @BeforeEach
    void setUp() {
        ConverterManager converterManager = new ConverterManager(applicationContext, converterXmlApplicationContextPath);
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
                converterTemplateId, null, null, Format.FHIR_SEARCH, null, null, null, null, null, Format.CSV);
        ExporterCoreParameters exporterCoreParameters = exporterCore.extractParameters(
                exporterParameters);
        Flux<Path> resultFlux = exporterCore.retrieveQuery(exporterCoreParameters, tokenContext);
        resultFlux.blockLast();
    }

    @Test
    void retrieveByQuery() {
    }

}
