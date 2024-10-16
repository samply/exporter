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

  private Path excelPath;

  private boolean isExcelFileAlreadyCreated = false;

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
    if (excelPath == null){
      String filename = converterTemplateUtils.replaceTokens(converterTemplate.getExcelFilename());
      excelPath = Paths.get(writeDirectory).resolve(filename);
    }
    return excelPath;
  }

  public boolean isExcelFileAlreadyCreated() {
    return isExcelFileAlreadyCreated;
  }

  public void setExcelFileAlreadyCreated(boolean excelFileAlreadyCreated) {
    isExcelFileAlreadyCreated = excelFileAlreadyCreated;
  }

}
