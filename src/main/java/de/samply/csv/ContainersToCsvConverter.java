package de.samply.csv;

import de.samply.converter.ConverterImpl;
import de.samply.container.Container;
import de.samply.container.Containers;
import de.samply.converter.Format;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.teiler.TeilerConst;
import de.samply.template.ConverterTemplateUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ContainersToCsvConverter extends ConverterImpl<Containers, Path> {

  private ConverterTemplateUtils converterTemplateUtils;
  private String writeDirectory;

  public ContainersToCsvConverter(@Autowired ConverterTemplateUtils converterTemplateUtils,
      @Value(TeilerConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory) {
    this.writeDirectory = writeDirectory;
    this.converterTemplateUtils = converterTemplateUtils;
  }

  @Override
  protected Flux<Path> convert(Containers containers, ConverterTemplate template) {
    Map<String, String> filenameMap = new HashMap<>();
    Flux<Path> pathFlux = Flux.empty();
    writeContainersInCsv(containers, template, filenameMap).forEach(
        path -> pathFlux.concatWithValues(path));
    return pathFlux;
  }

  public List<Path> writeContainersInCsv(Containers containers, ConverterTemplate template,
      Map<String, String> filenameMap) {
    List<Path> pathList = new ArrayList<>();
    template.getContainerTemplates().forEach(containerTemplate ->
        pathList.add(writeContainersInCsv(containers.getContainers(containerTemplate),
            template, containerTemplate, filenameMap))
    );
    return pathList;
  }

  private Path writeContainersInCsv(List<Container> containers,
      ConverterTemplate converterTemplate, ContainerTemplate containerTemplate,
      Map<String, String> filenameMap) {
    try {
      return writeContainersInCsvWithoutExceptionManagement(containers, converterTemplate,
          containerTemplate, filenameMap);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Path writeContainersInCsvWithoutExceptionManagement(List<Container> containers,
      ConverterTemplate converterTemplate, ContainerTemplate containerTemplate,
      Map<String, String> filenameMap) throws IOException {

    Path filePath = getFilePath(containerTemplate, filenameMap);
    boolean headersExists = headersExists(filePath);
    Files.write(filePath,
        new ContainerCsvWriterIterable(containers, converterTemplate, containerTemplate,
            headersExists),
        (headersExists) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    return filePath;

  }

  private Path getFilePath(ContainerTemplate containerTemplate, Map<String, String> filenameMap) {
    String filename = filenameMap.get(containerTemplate.getCsvFilename());
    if (filename == null) {
      filename = converterTemplateUtils.replaceTokens(containerTemplate.getCsvFilename());
      filenameMap.put(containerTemplate.getCsvFilename(), filename);
    }
    return Paths.get(writeDirectory).resolve(filename);
  }

  private boolean headersExists(Path filePath) {
    return Files.exists(filePath) && filePath.toFile().length() > 0;
  }

  @Override
  public Format getInputFormat() {
    return Format.CONTAINERS;
  }

  @Override
  public Format getOutputFormat() {
    return Format.CSV;
  }

}
