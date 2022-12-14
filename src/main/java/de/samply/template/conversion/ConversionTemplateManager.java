package de.samply.template.conversion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.samply.teiler.TeilerConst;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConversionTemplateManager {

  private Map<String, ConversionTemplate> idConverterTemplateMap = new HashMap<>();

  public ConversionTemplateManager(
      @Value(TeilerConst.CONVERTER_TEMPLATE_DIRECTORY_SV) String templateDirectory) {
    loadTemplates(Paths.get(templateDirectory));
  }

  private void loadTemplates(Path templateDirectory) {
    try {
      loadTemplatesWithoutExceptionmanagement(templateDirectory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadTemplatesWithoutExceptionmanagement(Path templateDirectory)
      throws IOException {
    if (Files.exists(templateDirectory)) {
      Files.list(templateDirectory).forEach(filePath -> loadTemplate(filePath));
    }
  }

  private void loadTemplate(Path templatePath) {
    try {
      loadTemplateWithoutExceptionManagement(templatePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadTemplateWithoutExceptionManagement(Path templatePath) throws IOException {
    ObjectMapper objectMapper =
        (templatePath.getFileName().toString().contains(".xml")) ? new XmlMapper()
            : new ObjectMapper();
    ConversionTemplate conversionTemplate = objectMapper.readValue(templatePath.toFile(),
        ConversionTemplate.class);
    idConverterTemplateMap.put(conversionTemplate.getId(), conversionTemplate);
  }

  public ConversionTemplate getConversionTemplate(String conversionTemplate) {
    return idConverterTemplateMap.get(conversionTemplate);
  }

}