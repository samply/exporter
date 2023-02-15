package de.samply.csv;

import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplateUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {

  private ConverterTemplateUtils converterTemplateUtils;
  private String writeDirectory;

  private List<Path> pathList = new ArrayList<>();

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

  public List<Path> getNewPaths(List<Path> pathList) {
    List<Path> results = new ArrayList<>();
    if (pathList != null && pathList.size() > 0) {
      pathList.forEach(path -> {
        if (isNew(path)) {
          results.add(path);
          this.pathList.add(path);
        }
      });
    }
    return results;
  }

  private boolean isNew(Path path){
    for (Path tempPath: pathList){
      if (tempPath.compareTo(path) == 0){
        return false;
      }
    }
    return true;
  }

}
