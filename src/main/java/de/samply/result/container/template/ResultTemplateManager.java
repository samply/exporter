package de.samply.result.container.template;

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
public class ResultTemplateManager {

  private Map<String, ResultTemplate> idContainersTemplateMap = new HashMap<>();

  public ResultTemplateManager(
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
    ResultTemplate resultTemplate = objectMapper.readValue(templatePath.toFile(),
        ResultTemplate.class);
    idContainersTemplateMap.put(resultTemplate.getId(), resultTemplate);
  }

  public ResultTemplate getResultTemplate(String resultTemplate) {
    return idContainersTemplateMap.get(resultTemplate);
  }

}
