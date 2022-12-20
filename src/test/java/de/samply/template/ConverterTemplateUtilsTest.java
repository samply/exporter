package de.samply.template;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConverterTemplateUtilsTest {

  @Test
  void replaceTokens() {
    String example = "example1";
    String result = ConverterTemplateUtils.replaceTokens(example);
    assertEquals(example, result);

    String part1 = "example-";
    example = part1 + "${TIMESTAMP}";
    result = ConverterTemplateUtils.replaceTokens(example);
    assertTrue(result.contains(part1) && !example.equals(result));

    String part2 = ".csv";
    example = part1 + "${TIMESTAMP}" + part2;
    result = ConverterTemplateUtils.replaceTokens(example);
    assertTrue(result.contains(part1) && result.contains(part2) && !example.equals(result));

    String format = "yyyyMMdd";
    example = part1 + "${TIMESTAMP:" + format + "}" + part2;
    result = ConverterTemplateUtils.replaceTokens(example);
    assertTrue(result.contains(part1) && result.contains(part2) && !example.equals(result)
        && result.length() == (part1 + part2 + format).length());

  }

}
