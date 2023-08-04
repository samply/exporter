package de.samply.csv;

import de.samply.container.Container;
import de.samply.converter.Format;
import de.samply.exporter.ExporterConst;
import de.samply.files.ContainerFileWriterIterable;
import de.samply.files.ContainersToFilesConverter;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContainersToCsvConverter extends ContainersToFilesConverter<Session> {


  public ContainersToCsvConverter(
      @Autowired ConverterTemplateUtils converterTemplateUtils,
      @Value(ExporterConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory) {
    super(converterTemplateUtils, writeDirectory);
  }

  @Override
  protected Session initializeSession(ConverterTemplate template) {
    return new Session(converterTemplateUtils, writeDirectory);
  }

  @Override
  protected ContainerFileWriterIterable createContainerFileWriterIterable(
      List<Container> containers, ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate, Session session) {
    return new ContainerCsvWriterIterable(containers, converterTemplate, containerTemplate);
  }

  @Override
  public Format getOutputFormat() {
    return Format.CSV;
  }

}
