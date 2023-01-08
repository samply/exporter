package de.samply.excel;

import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class Session {


  private ConverterTemplateUtils converterTemplateUtils;
  private String writeDirectory;
  private Integer workbookWindow;
  private Workbook workbook;

  public Session(ConverterTemplateUtils converterTemplateUtils, String writeDirectory,
      Integer workbookWindow) {
    this.converterTemplateUtils = converterTemplateUtils;
    this.writeDirectory = writeDirectory;
    this.workbookWindow = workbookWindow;
  }

  public Workbook fetchWorkbook() {
    if (workbook == null) {
      this.workbook = new SXSSFWorkbook(workbookWindow);
    }
    return workbook;
  }

  public Path getExcelPath(ConverterTemplate converterTemplate) {
    String filename = converterTemplateUtils.replaceTokens(converterTemplate.getExcelFilename());
    return Paths.get(writeDirectory).resolve(filename);
  }

}
