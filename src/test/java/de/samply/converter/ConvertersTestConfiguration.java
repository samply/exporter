package de.samply.converter;

import de.samply.EnvironmentTestUtils;
import de.samply.csv.ContainersToCsvConverter;
import de.samply.excel.ContainersToExcelConverter;
import de.samply.exporter.ExporterConst;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.json.ContainersToJsonConverter;
import de.samply.template.ConverterTemplateUtils;
import de.samply.utils.EnvironmentUtils;
import de.samply.xml.ContainersToXmlConverter;
import org.apache.poi.ss.SpreadsheetVersion;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ConvertersTestConfiguration {

    private final String OUTPUT_DIRECTORY = "./output";
    private String fhirPackagesDirectory = "./fhir-packages";
    private BundleToContainersConverter bundleToContainersConverter;
    private ContainersToCsvConverter containersToCsvConverter;
    private ContainersToExcelConverter containersToExcelConverter;
    private ContainersToJsonConverter containersToJsonConverter;
    private ContainersToXmlConverter containersToXmlConverter;

    public ConvertersTestConfiguration() {
        EnvironmentUtils environmentUtils = new EnvironmentUtils(
                EnvironmentTestUtils.getEmptyMockEnvironment());
        this.bundleToContainersConverter = new BundleToContainersConverter(
                fhirPackagesDirectory, false);
        ConverterTemplateUtils converterTemplateUtils = new ConverterTemplateUtils(
                ExporterConst.DEFAULT_TIMESTAMP_FORMAT, environmentUtils);
        this.containersToCsvConverter = new ContainersToCsvConverter(converterTemplateUtils,
                OUTPUT_DIRECTORY);
        this.containersToExcelConverter = new ContainersToExcelConverter(converterTemplateUtils, 30000000
                , OUTPUT_DIRECTORY, SpreadsheetVersion.EXCEL2007.getMaxRows());
        this.containersToJsonConverter = new ContainersToJsonConverter(converterTemplateUtils,
                OUTPUT_DIRECTORY);
        this.containersToXmlConverter = new ContainersToXmlConverter(converterTemplateUtils,
                OUTPUT_DIRECTORY);
    }

    @Bean
    public BundleToContainersConverter getBundleToContainersConverter() {
        return bundleToContainersConverter;
    }

    @Bean
    public ContainersToCsvConverter getContainersToCsvConverter() {
        return containersToCsvConverter;
    }

    @Bean
    public ContainersToExcelConverter getContainersToExcelConverter() {
        return containersToExcelConverter;
    }

    @Bean
    public ContainersToJsonConverter getContainersToJsonConverter() {
        return containersToJsonConverter;
    }

    @Bean
    public ContainersToXmlConverter getContainersToXmlConverter() {
        return containersToXmlConverter;
    }

}
