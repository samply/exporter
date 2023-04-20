package de.samply.files;

import de.samply.container.Container;
import de.samply.container.Containers;
import de.samply.converter.ConverterImpl;
import de.samply.converter.Format;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public abstract class ContainersToFilesConverter<S extends Session> extends
    ConverterImpl<Containers, Path, S> {

  private final static Logger logger = LoggerFactory.getLogger(ContainersToFilesConverter.class);

  protected ConverterTemplateUtils converterTemplateUtils;
  protected String writeDirectory;

  public ContainersToFilesConverter(ConverterTemplateUtils converterTemplateUtils,
      String writeDirectory) {
    this.converterTemplateUtils = converterTemplateUtils;
    this.writeDirectory = writeDirectory;
  }

  @Override
  public Format getInputFormat() {
    return Format.CONTAINERS;
  }

  @Override
  protected Flux<Path> convert(Containers containers, ConverterTemplate template, S session) {
    return Flux.fromIterable(
        session.getNewPaths(writeContainersInFile(containers, template, session)));
  }

  public List<Path> writeContainersInFile(Containers containers, ConverterTemplate template,
      S session) {
    List<Path> pathList = new ArrayList<>();
    template.getContainerTemplates().forEach(containerTemplate ->
        pathList.add(writeContainersInFile(containers.getContainers(containerTemplate), template,
            containerTemplate, session))
    );
    return pathList;
  }

  private Path writeContainersInFile(List<Container> containers,
      ConverterTemplate converterTemplate, ContainerTemplate containerTemplate, S session) {
    try {
      return writeContainersInFileWithoutExceptionHandling(containers, converterTemplate,
          containerTemplate, session);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Path writeContainersInFileWithoutExceptionHandling(List<Container> containers,
      ConverterTemplate converterTemplate, ContainerTemplate containerTemplate, S session)
      throws IOException {
    Path filePath = session.getFilePath(containerTemplate);
    boolean isFileAlreadyInitialized = isFileAlreadyInitialized(filePath);
    Files.write(filePath,
        createContainerFileWriterIterable(containers, converterTemplate, containerTemplate,
            session),
        (isFileAlreadyInitialized) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    return filePath;
  }

  protected boolean isFileAlreadyInitialized(Path filePath) {
    return Files.exists(filePath) && filePath.toFile().length() > 0;
  }

  protected abstract ContainerFileWriterIterable createContainerFileWriterIterable(
      List<Container> containers, ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate, S session);


  @Override
  protected Runnable getSessionCompleter(ConverterTemplate template, S session) {
    return () -> {
      template.getContainerTemplates()
          .forEach(containerTemplate -> completeFileWithExceptionHandling(containerTemplate,
              session.getFilePath(containerTemplate)));
    };
  }

  private void completeFileWithExceptionHandling(ContainerTemplate containerTemplate,
      Path filePath) {
    try {
      completeFile(containerTemplate, filePath);
    } catch (IOException e) {
      logger.error(ExceptionUtils.getStackTrace(e));
      throw new RuntimeException(e);
    }
  }

  protected void completeFile(ContainerTemplate containerTemplate, Path filePath)
      throws IOException {
  }

}
