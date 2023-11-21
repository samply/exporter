package de.samply.fhir;

import de.samply.EnvironmentTestUtils;
import de.samply.container.Containers;
import de.samply.csv.ContainersToCsvConverter;
import de.samply.csv.Session;
import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import de.samply.template.ConverterTemplateUtils;
import de.samply.template.token.TokenContext;
import de.samply.utils.EnvironmentUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.nio.file.Path;

@Disabled
class FhirSearchQueryToBundleConverterTest {

    private FhirSearchQueryConverter fhirSearchQueryToBundleConverter;
    private ConverterTemplateManager converterTemplateManager;
    private BundleToContainersConverter bundleToContainersConverter;
    private BundleToContainersConverterSession bundleToContainersConverterSession;
    private ContainersToCsvConverter containersToCsvConverter;
    private Session containersToCsvConverterSession;

    private String blazeStoreUrl = "http://localhost:8091/fhir";
    private String sourceId = "blazeStore";
    private String templateDirectory = "./templates";
    private String outputDirectory = "./output";
    private String fhirPackagesDirectory = "./fhir-packages";
    private String templateId = "test-template1";
    private TokenContext tokenContext = new TokenContext(null);

    @BeforeEach
    void setUp() {
        this.fhirSearchQueryToBundleConverter = new FhirSearchQueryConverter(blazeStoreUrl, sourceId);
        EnvironmentUtils environmentUtils = new EnvironmentUtils(
                EnvironmentTestUtils.getEmptyMockEnvironment());
        ConverterTemplateUtils converterTemplateUtils = new ConverterTemplateUtils(
                ExporterConst.DEFAULT_TIMESTAMP_FORMAT, environmentUtils);
        this.containersToCsvConverter = new ContainersToCsvConverter(converterTemplateUtils,
                outputDirectory, " ");
        this.containersToCsvConverterSession = new Session(converterTemplateUtils, outputDirectory, tokenContext);
        this.converterTemplateManager = new ConverterTemplateManager(templateDirectory);
        this.bundleToContainersConverter = new BundleToContainersConverter(fhirPackagesDirectory, false);
        this.bundleToContainersConverterSession = new BundleToContainersConverterSession();
    }

    @Test
    void testExecute() {
        ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate(templateId);
        fhirSearchQueryToBundleConverter.convert(Flux.just("Patient"), converterTemplate, tokenContext)
                .subscribe(bundle -> {
                    Containers containers = bundleToContainersConverter.convertToContainers(bundle,
                            converterTemplate, bundleToContainersConverterSession);
                    containersToCsvConverter.writeContainersInFile(containers, converterTemplate,
                            containersToCsvConverterSession);
                });
        //TODO
    }

    @Test
    void testConverters() {
        ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate(templateId);
        Flux<String> stringFlux = Flux.just("Patient");
        Flux<Bundle> bundleFlux = fhirSearchQueryToBundleConverter.convert(stringFlux, converterTemplate, null);
        Flux<Containers> containersFlux = bundleToContainersConverter.convert(bundleFlux, converterTemplate, tokenContext);
        Flux<Path> pathFlux = containersToCsvConverter.convert(containersFlux, converterTemplate, tokenContext);
        pathFlux.blockLast();
        //TODO
    }

}
