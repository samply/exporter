package de.samply.files;

import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplateUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Session {

  private ConverterTemplateUtils converterTemplateUtils;
  private String writeDirectory;
  private List<Path> pathList = new ArrayList<>();
  private Map<ContainerTemplate, Path> containerTemplatePathMap = new HashMap<>();

  public Session(ConverterTemplateUtils converterTemplateUtils,
      String writeDirectory) {
    this.converterTemplateUtils = converterTemplateUtils;
    this.writeDirectory = writeDirectory;
  }


  public Path getFilePath(ContainerTemplate containerTemplate) {
    Path path = containerTemplatePathMap.get(containerTemplate);
    if (path == null) {
      String filename = converterTemplateUtils.replaceTokens(getFilename(containerTemplate));
      path = Paths.get(writeDirectory).resolve(filename);
      containerTemplatePathMap.put(containerTemplate, path);
    }
    return path;
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

  private boolean isNew(Path path) {
    for (Path tempPath : pathList) {
      if (tempPath.compareTo(path) == 0) {
        return false;
      }
    }
    return true;
  }

  protected abstract String getFilename(ContainerTemplate containerTemplate);

}
