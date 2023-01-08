package de.samply.excel;

import de.samply.container.Containers;
import de.samply.converter.ConverterImpl;
import de.samply.converter.Format;
import de.samply.teiler.TeilerConst;
import de.samply.template.ConverterTemplate;
import java.nio.file.Path;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ContainersToExcelConverter extends ConverterImpl<Containers, Path, Session> {

  private String writeDirectory;

  public ContainersToExcelConverter(
      @Value(TeilerConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory) {
    this.writeDirectory = writeDirectory;
  }

  @Override
  public Format getInputFormat() {
    return Format.CONTAINERS;
  }

  @Override
  public Format getOutputFormat() {
    return Format.EXCEL;
  }

  @Override
  protected Flux<Path> convert(Containers input, ConverterTemplate template, Session session) {
    return null;
  }

  @Override
  protected Session initializeSession() {
    return new Session(writeDirectory);
  }

  private Workbook openWorkbook(ConverterTemplate converterTemplate) {
    String excelFilename = converterTemplate.getExcelFilename();

    //TODO
    return null;
  }

}
