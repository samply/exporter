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

import de.samply.template.token.TokenContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContainersToCsvConverter extends ContainersToFilesConverter<Session> {

  private final String csvSeparatorReplacement;

  public ContainersToCsvConverter(
      @Autowired ConverterTemplateUtils converterTemplateUtils,
      @Value(ExporterConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory,
      @Value(ExporterConst.CSV_SEPARATOR_REPLACEMENT_SV) String csvSeparatorReplacement) {
    super(converterTemplateUtils, writeDirectory);
    this.csvSeparatorReplacement = csvSeparatorReplacement;
  }

  @Override
  protected Session initializeSession(ConverterTemplate template, TokenContext tokenContext) {
    return new Session(converterTemplateUtils, writeDirectory, tokenContext);
  }

  @Override
  protected ContainerFileWriterIterable createContainerFileWriterIterable(
      List<Container> containers, ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate, Session session) {
    return new ContainerCsvWriterIterable(containers, converterTemplate, containerTemplate, csvSeparatorReplacement);
  }

  @Override
  public Format getOutputFormat() {
    return Format.CSV;
  }

}
