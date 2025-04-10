package de.samply.template;

import de.samply.EnvironmentTestUtils;
import de.samply.exporter.ExporterConst;
import de.samply.utils.EnvironmentUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ConverterTemplateUtilsTest {

    private ConverterTemplateUtils converterTemplateUtils;
    private String property1 = "PROPERTY1";
    private String value1 = "VALUE1";
    private String property2 = "PROPERTY2";
    private String value2 = "VALUE2";
    private String property3 = "PROPERTY3";
    private String value3 = "VALUE3";


    @BeforeEach
    void setUp() {
        Map<String, Object> properties = generateProperties();
        ConfigurableEnvironment environment = EnvironmentTestUtils.getMockEnvironment(properties);
        EnvironmentUtils environmentUtils = new EnvironmentUtils(environment);
        this.converterTemplateUtils = new ConverterTemplateUtils(ExporterConst.DEFAULT_TIMESTAMP_FORMAT, ExporterConst.DEFAULT_SITE, environmentUtils);
    }

    private Map<String, Object> generateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(property1, value1);
        properties.put(property2, value2);
        properties.put(property3, value3);
        return properties;
    }

    @Test
    void replaceTokens() {
        String example = "example1";
        String result = converterTemplateUtils.replaceTokens(example);
        assertEquals(example, result);

        String part1 = "example-";
        example = part1 + ExporterConst.TOKEN_HEAD + "TIMESTAMP" + ExporterConst.TOKEN_END;
        result = converterTemplateUtils.replaceTokens(example);
        assertTrue(result.contains(part1) && !example.equals(result));

        String part2 = ".csv";
        example = part1 + ExporterConst.TOKEN_HEAD + "TIMESTAMP" + ExporterConst.TOKEN_END + part2;
        result = converterTemplateUtils.replaceTokens(example);
        assertTrue(result.contains(part1) && result.contains(part2) && !example.equals(result));

        String format = "yyyyMMdd";
        example =
                part1 + ExporterConst.TOKEN_HEAD + "TIMESTAMP" + ExporterConst.TOKEN_EXTENSION_DELIMITER
                        + format + ExporterConst.TOKEN_END + part2;
        result = converterTemplateUtils.replaceTokens(example);
        assertTrue(result.contains(part1) && result.contains(part2) && !example.equals(result)
                && result.length() == (part1 + part2 + format).length());

        example = part1 + ExporterConst.TOKEN_HEAD + property1 + ExporterConst.TOKEN_END + "-"
                + ExporterConst.TOKEN_HEAD + property2 + ExporterConst.TOKEN_END + part2;

        result = converterTemplateUtils.replaceTokens(example);
        assertEquals(part1 + value1 + "-" + value2 + part2, result);

        String notDefinedProperty = "NOT_DEFINED";
        example = part1 + ExporterConst.TOKEN_HEAD + property1 + ExporterConst.TOKEN_END + "-"
                + ExporterConst.TOKEN_HEAD + notDefinedProperty + ExporterConst.TOKEN_END + part2;

        result = converterTemplateUtils.replaceTokens(example);
        assertEquals(
                part1 + value1 + "-" + part2, result);

    }

}
