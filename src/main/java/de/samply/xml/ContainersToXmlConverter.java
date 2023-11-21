package de.samply.xml;

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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContainersToXmlConverter extends ContainersToFilesConverter<Session> {

  public ContainersToXmlConverter(
      @Autowired ConverterTemplateUtils converterTemplateUtils,
      @Value(ExporterConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory) {
    super(converterTemplateUtils, writeDirectory);
  }

  @Override
  public Format getOutputFormat() {
    return Format.XML;
  }

  @Override
  protected Session initializeSession(ConverterTemplate converterTemplate, HttpServletRequest httpServletRequest) {
    return new Session(converterTemplateUtils, writeDirectory, httpServletRequest);
  }

  @Override
  protected ContainerFileWriterIterable createContainerFileWriterIterable(
      List<Container> containers, ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate, Session session) {
    return new ContainerXmlWriterIterable(containers, converterTemplate, containerTemplate);
  }

  @Override
  protected void completeFile(ContainerTemplate containerTemplate, Path filePath)
      throws IOException {
    if (isFileAlreadyInitialized(filePath)) {
      Files.write(filePath, ("</" + containerTemplate.getXmlRootElement() + ">").getBytes(),
          StandardOpenOption.APPEND);
    }
  }


}
