package de.samply.excel;

import de.samply.container.Container;
import de.samply.container.Containers;
import de.samply.converter.ConverterImpl;
import de.samply.converter.Format;
import de.samply.exporter.ExporterConst;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ContainersToExcelConverter extends ConverterImpl<Containers, Path, Session> {

  private String writeDirectory;
  private ConverterTemplateUtils converterTemplateUtils;
  private Integer workbookWindow;

  public ContainersToExcelConverter(
      @Value(ExporterConst.EXCEL_WORKBOOK_WINDOW_SV) Integer workbookWindow,
      @Autowired ConverterTemplateUtils converterTemplateUtils,
      @Value(ExporterConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory) {
    this.workbookWindow = workbookWindow;
    this.writeDirectory = writeDirectory;
    this.converterTemplateUtils = converterTemplateUtils;
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
    return Flux.generate(
        sync -> {
          addConvertersToWorkbook(input, template, session);
          if (!session.isExcelFileAlreadyCreated()){
            sync.next(session.getExcelPath(template));
            session.setExcelFileAlreadyCreated(true);
          }
          sync.complete();
        }
    );
  }

  private void addConvertersToWorkbook(Containers input, ConverterTemplate template,
      Session session) {
    template.getContainerTemplates().forEach(containerTemplate -> addContainersToWorkbook(
        template, containerTemplate, session, input.getContainers(containerTemplate)));
  }

  private void addContainersToWorkbook(ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate, Session session, List<Container> containers) {
    Sheet sheet = fetchSheet(containerTemplate, session);
    AtomicInteger rowIndex = new AtomicInteger(sheet.getLastRowNum() + 1);
    containers.forEach(container -> {
      Row row = sheet.createRow(rowIndex.getAndIncrement());
      addContainerToRow(containerTemplate, row, container);
    });
  }

  private void addContainerToRow(ContainerTemplate containerTemplate, Row row,
      Container container) {
    AtomicInteger columnIndex = new AtomicInteger(0);
    containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> {
      String attributeValue = container.getAttributeValue(attributeTemplate);
      row.createCell(columnIndex.getAndIncrement())
          .setCellValue((attributeValue != null) ? attributeValue : ExporterConst.EMPTY_EXCEL_CELL);
    });
  }

  private Sheet fetchSheet(ContainerTemplate containerTemplate, Session session) {
    String safeSheetName = WorkbookUtil.createSafeSheetName(containerTemplate.getExcelSheet());
    Sheet sheet = session.fetchWorkbook().getSheet(safeSheetName);
    if (sheet == null) {
      sheet = session.fetchWorkbook().createSheet(safeSheetName);
      if (sheet instanceof SXSSFSheet) {
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
      }
      createHeaderRow(sheet, containerTemplate);
    }
    return sheet;
  }

  private void createHeaderRow(Sheet sheet, ContainerTemplate containerTemplate) {
    Row row = sheet.createRow(0);
    AtomicInteger counter = new AtomicInteger(0);
    containerTemplate.getAttributeTemplates().forEach(attributeTemplate ->
        row.createCell(counter.getAndIncrement())
            .setCellValue(attributeTemplate.getExcelColumnName()));
  }

  @Override
  protected Session initializeSession() {
    return new Session(converterTemplateUtils, writeDirectory, workbookWindow);
  }

  @Override
  protected Runnable getSessionCompleter(ConverterTemplate template, Session session) {
    return () -> {
      autoSizeWorkbook(template, session);
      addAutoFilter(session);
      freezeHeaderInWorkbook(session);
      boldHeaderRow(session);
      writeWorkbook(template, session);
    };
  }

  private void autoSizeWorkbook(ConverterTemplate template, Session session) {
    Workbook workbook = session.fetchWorkbook();
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
      Sheet sheet = workbook.getSheetAt(i);
      Row headerRow = sheet.getRow(0);
      if (headerRow != null) {
        for (int j = 0; j <= headerRow.getLastCellNum(); j++) {
          sheet.autoSizeColumn(j);
        }
      }
    }
  }

  private void freezeHeaderInWorkbook(Session session) {
    Workbook workbook = session.fetchWorkbook();
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
      Sheet sheet = workbook.getSheetAt(i);
      sheet.createFreezePane(0, 1);
    }
  }

  private void addAutoFilter(Session session) {
    Workbook workbook = session.fetchWorkbook();
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
      addAutoFilter(workbook, i);
    }
  }

  private void addAutoFilter(Workbook workbook, int sheetIndex) {
    Sheet sheet = workbook.getSheetAt(sheetIndex);
    int rowStartIndex = 0;
    int rowEndIndex = sheet.getLastRowNum();

    int columnStartIndex = 0;
    int columnEndIndex = sheet.getRow(0).getLastCellNum() - 1;

    CellRangeAddress cra = new CellRangeAddress(rowStartIndex, rowEndIndex, columnStartIndex,
        columnEndIndex);
    sheet.setAutoFilter(cra);
  }

  private void boldHeaderRow(Session session) {
    Workbook workbook = session.fetchWorkbook();
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
      CellStyle cellStyle = workbook.createCellStyle();
      Font font = workbook.createFont();
      font.setBold(true);
      cellStyle.setFont(font);
      Row titleRow = workbook.getSheetAt(i).getRow(0);

      for (int j = 0; j < titleRow.getLastCellNum(); j++) {
        Cell cell = titleRow.getCell(j);
        cell.setCellStyle(cellStyle);
      }
    }
  }


  private void writeWorkbook(ConverterTemplate template, Session session) {
    try (FileOutputStream fileOutputStream = new FileOutputStream(
        session.getExcelPath(template).toString())) {
      session.fetchWorkbook().write(fileOutputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
