package de.samply.json;

import de.samply.container.Container;
import de.samply.converter.Format;
import de.samply.exporter.ExporterConst;
import de.samply.files.ContainerFileWriterIterable;
import de.samply.files.ContainersToFilesConverter;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContainersToJsonConverter extends ContainersToFilesConverter<Session> {

  public ContainersToJsonConverter(
      @Autowired ConverterTemplateUtils converterTemplateUtils,
      @Value(ExporterConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory) {
    super(converterTemplateUtils, writeDirectory);
  }

  @Override
  public Format getOutputFormat() {
    return Format.JSON;
  }

  @Override
  protected Session initializeSession(ConverterTemplate converterTemplate) {
    return new Session(converterTemplateUtils, writeDirectory);
  }

  @Override
  protected ContainerFileWriterIterable createContainerFileWriterIterable(
      List<Container> containers, ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate, Session session) {
    Path filePath = session.getFilePath(containerTemplate);
    boolean isFirstLine = !isFileAlreadyInitialized(filePath);
    return new ContainerJsonWriterIterable(containers, converterTemplate,
        containerTemplate).setIfIsFirstLine(isFirstLine);
  }

  @Override
  protected void completeFile(ContainerTemplate containerTemplate, Path filePath)
      throws IOException {
    if (isFileAlreadyInitialized(filePath)){
      removeLastComma(filePath);
      Files.write(filePath, "]}".getBytes(), StandardOpenOption.APPEND);
    }
  }

  private void removeLastComma(Path filePath){

  }

}
