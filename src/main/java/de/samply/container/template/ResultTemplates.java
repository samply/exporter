package de.samply.container.template;

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
public class ResultTemplates {

  private Map<String, ContainersTemplate> idContainersTemplateMap = new HashMap<>();

  public ResultTemplates(
      @Value(TeilerConst.RESULT_TEMPLATE_DIRECTORY_SV) String templateDirectory) {
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
    ContainersTemplate containersTemplate = objectMapper.readValue(templatePath.toFile(),
        ContainersTemplate.class);
    idContainersTemplateMap.put(containersTemplate.getId(), containersTemplate);
  }

  public ContainersTemplate getResultTemplate(String resultTemplate) {
    return idContainersTemplateMap.get(resultTemplate);
  }

}
