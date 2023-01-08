package de.samply.csv;

import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplateUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Session {

  private ConverterTemplateUtils converterTemplateUtils;
  private String writeDirectory;

  public Session(ConverterTemplateUtils converterTemplateUtils,
      String writeDirectory) {
    this.converterTemplateUtils = converterTemplateUtils;
    this.writeDirectory = writeDirectory;
  }

  private Map<String, String> filenameMap = new HashMap<>();

  public Path getFilePath(ContainerTemplate containerTemplate) {
    String filename = filenameMap.get(containerTemplate.getCsvFilename());
    if (filename == null) {
      filename = converterTemplateUtils.replaceTokens(containerTemplate.getCsvFilename());
      filenameMap.put(containerTemplate.getCsvFilename(), filename);
    }
    return Paths.get(writeDirectory).resolve(filename);
  }

}
