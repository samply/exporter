package de.samply.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.samply.teiler.TeilerConst;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SourcesManager {

  private final Map<String, Source> idSourceMap;

  public SourcesManager(@Value(TeilerConst.SOURCES_PATH_SV) String sourcesPath) {
    this.idSourceMap = fetchSources(Paths.get(sourcesPath));
  }

  private Map<String, Source> fetchSources(Path sourcesPath) {
    try {
      return fetchSourcesWithoutExceptionManagement(sourcesPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, Source> fetchSourcesWithoutExceptionManagement(Path sourcesPath)
      throws IOException {
    Map<String, Source> idSourceMap = new HashMap<>();
    String filename = sourcesPath.getFileName().toString();
    ObjectMapper objectMapper = (filename.contains(".xml")) ? new XmlMapper() : new ObjectMapper();
    objectMapper.readValue(sourcesPath.toFile(), Sources.class).getSources()
        .forEach(source -> idSourceMap.put(source.getId(), source));
    return idSourceMap;
  }

  public Source getSource (String sourceId){
    return idSourceMap.get(sourceId);
  }

}
