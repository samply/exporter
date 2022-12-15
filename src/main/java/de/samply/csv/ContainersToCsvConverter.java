package de.samply.csv;

import de.samply.converter.ConverterImpl;
import de.samply.container.Container;
import de.samply.container.Containers;
import de.samply.converter.Format;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.teiler.TeilerConst;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ContainersToCsvConverter extends ConverterImpl<Containers, Path> {

  private String writeDirectory;

  public ContainersToCsvConverter(@Value(TeilerConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory) {
    this.writeDirectory = writeDirectory;
  }

  @Override
  protected Flux<Path> convert(Containers containers, ConverterTemplate template) {
    Flux<Path> pathFlux = Flux.empty();
    writeContainersInCsv(containers, template).forEach(path -> pathFlux.concatWithValues(path));
    return pathFlux;
  }

  public List<Path> writeContainersInCsv(Containers containers, ConverterTemplate template) {
    List<Path> pathList = new ArrayList<>();
    template.getContainerTemplates().forEach(containerTemplate ->
        pathList.add(writeContainersInCsv(containers.getContainers(containerTemplate),
            template, containerTemplate))
    );
    return pathList;
  }

  private Path writeContainersInCsv(List<Container> containers,
      ConverterTemplate converterTemplate, ContainerTemplate containerTemplate) {
    try {
      return writeContainersInCsvWithoutExceptionManagement(containers, converterTemplate, containerTemplate);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Path writeContainersInCsvWithoutExceptionManagement(List<Container> containers,
      ConverterTemplate converterTemplate, ContainerTemplate containerTemplate) throws IOException {

    Path filePath = getFilePath(containerTemplate.getCsvFilename());
    boolean headersExists = headersExists(filePath);
    Files.write(filePath,
        new ContainerCsvWriterIterable(containers, converterTemplate, containerTemplate,
            headersExists),
        (headersExists) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    return filePath;

  }

  private Path getFilePath(String filename) {
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
