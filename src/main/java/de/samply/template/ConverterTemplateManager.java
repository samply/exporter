package de.samply.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.samply.exporter.ExporterConst;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ConverterTemplateManager {

  private Map<String, ConverterTemplate> idConverterTemplateMap = new HashMap<>();
  private Map<String, Path> idConverterTemplatePathMap = new HashMap<>();

  public ConverterTemplateManager(
      @Value(ExporterConst.CONVERTER_TEMPLATE_DIRECTORY_SV) String templateDirectory) {
    loadTemplates(Paths.get(templateDirectory));
  }

  private void loadTemplates(Path templateDirectory) {
    try {
      loadTemplatesWithoutExceptionHandling(templateDirectory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadTemplatesWithoutExceptionHandling(Path templateDirectory)
      throws IOException {
    if (Files.exists(templateDirectory)) {
      Files.list(templateDirectory).filter(path -> !Files.isDirectory(path))
          .forEach(filePath -> loadTemplate(filePath));
    }
  }

  private void loadTemplate(Path templatePath) {
    try {
      loadTemplateWithoutExceptionHandling(templatePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadTemplateWithoutExceptionHandling(Path templatePath) throws IOException {
    ConverterTemplate converterTemplate = fetchConverterTemplate(templatePath);
    idConverterTemplateMap.put(converterTemplate.getId(), converterTemplate);
    idConverterTemplatePathMap.put(converterTemplate.getId(), templatePath);
  }

  public ConverterTemplate fetchConverterTemplate(Path templatePath) throws IOException {
    String contentType =
        (templatePath.getFileName().toString().contains(".xml")) ? MediaType.APPLICATION_XML_VALUE
            : MediaType.APPLICATION_JSON_VALUE;
    return fetchConverterTemplate(Files.readString(templatePath), contentType);
  }

  public ConverterTemplate fetchConverterTemplate(String template, String contentType)
      throws IOException {
    ObjectMapper objectMapper =
        (contentType.equalsIgnoreCase(MediaType.APPLICATION_XML_VALUE)) ?
            new XmlMapper() : new ObjectMapper();
    return objectMapper.readValue(template, ConverterTemplate.class);
  }

  public ConverterTemplate getConverterTemplate(String converterTemplateId) {
    return idConverterTemplateMap.get(converterTemplateId);
  }

  public Set<String> getConverterTemplateIds() {
    return idConverterTemplateMap.keySet();
  }

  public Optional<Path> getTemplatePath (String templateId){
    return Optional.ofNullable(idConverterTemplatePathMap.get(templateId));
  }

}
