package de.samply.csv;

import de.samply.container.Container;
import de.samply.container.Containers;
import de.samply.converter.ConverterImpl;
import de.samply.converter.Format;
import de.samply.exporter.ExporterConst;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ContainersToCsvConverter extends ConverterImpl<Containers, Path, Session> {

  private ConverterTemplateUtils converterTemplateUtils;
  private String writeDirectory;

  public ContainersToCsvConverter(@Autowired ConverterTemplateUtils converterTemplateUtils,
      @Value(ExporterConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory) {
    this.writeDirectory = writeDirectory;
    this.converterTemplateUtils = converterTemplateUtils;
  }

  @Override
  protected Flux<Path> convert(Containers containers, ConverterTemplate template, Session session) {
    return Flux.fromIterable(
        session.getNewPaths(writeContainersInCsv(containers, template, session)));
  }

  @Override
  protected Session initializeSession(ConverterTemplate template) {
    return new Session(converterTemplateUtils, writeDirectory);
  }

  public List<Path> writeContainersInCsv(Containers containers, ConverterTemplate template,
      Session session) {
    List<Path> pathList = new ArrayList<>();
    template.getContainerTemplates().forEach(containerTemplate ->
        pathList.add(writeContainersInCsv(containers.getContainers(containerTemplate), template,
            containerTemplate, session))
    );
    return pathList;
  }

  private Path writeContainersInCsv(List<Container> containers,
      ConverterTemplate converterTemplate, ContainerTemplate containerTemplate, Session session) {
    try {
      return writeContainersInCsvWithoutExceptionHandling(containers, converterTemplate,
          containerTemplate, session);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Path writeContainersInCsvWithoutExceptionHandling(List<Container> containers,
      ConverterTemplate converterTemplate, ContainerTemplate containerTemplate, Session session)
      throws IOException {

    Path filePath = session.getFilePath(containerTemplate);
    boolean headersExists = headersExists(filePath);
    Files.write(filePath,
        new ContainerCsvWriterIterable(containers, converterTemplate, containerTemplate,
            headersExists),
        (headersExists) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    return filePath;

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
