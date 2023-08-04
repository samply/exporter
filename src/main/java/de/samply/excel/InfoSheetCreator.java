package de.samply.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;

public class InfoSheetCreator {


  /*
  private String siteName;

  public void createInfoSheet(Workbook workBook) {
    CellStyle cs = workBook.createCellStyle();
    cs.setWrapText(true);

    // Create a new font and alter it.
    Font headerFont = workBook.createFont();
    headerFont.setFontHeightInPoints((short) 24);
    headerFont.setBold(true);
    CellStyle headerStyle = workBook.createCellStyle();
    headerStyle.setFont(headerFont);

    Sheet infoSheet = workBook.createSheet("Info");
    if (infoSheet instanceof SXSSFSheet) {
      ((SXSSFSheet)infoSheet).trackAllColumnsForAutoSizing();
    }

    Row headerRow = infoSheet.createRow(0);
    headerRow.setHeightInPoints(24);
    Cell headerCell = headerRow.createCell(0);
    headerCell.setCellStyle(headerStyle);
    headerCell.setCellValue("Daten-Export aus Brückenkopf");

    Row contentRow = infoSheet.createRow(1);
    ;
    contentRow.setHeightInPoints(14 * infoSheet.getDefaultRowHeightInPoints());

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(
        "Dies ist ein automatischer Datenexport aus dem DKTK-Brückenkopf am Standort " + siteName
            + ", der am " + getCurrentTimestamp()
            + " zur Beantwortung der folgenden Suchanfrage erzeugt wurde:\n\n");
    stringBuilder.append("Name der Anfrage: " + inquiry.getLabel() + "\n");
    if (queryResult instanceof QueryResult) {
      QueryResult queryResult1 = (QueryResult) queryResult;
      stringBuilder.append("Anzahl der Ergebnisse: " + queryResult1.getPatient().size() + "\n");
    } else if (queryResult instanceof BbmriResult) {
      BbmriResult queryResult1 = (BbmriResult) queryResult;
      stringBuilder.append("Anzahl der Ergebnisse: " + queryResult1.getDonors().size() + "\n");
    }
    stringBuilder.append("Kontaktperson: ");
    if (contact.getTitle() != null && contact.getTitle().length() > 0) {
      stringBuilder.append(contact.getTitle() + " ");
    }
    stringBuilder.append(contact.getFirstname() + " " + contact.getLastname() + "\n");
    stringBuilder.append("Beschreibung: " + inquiry.getDescription() + "\n");
    stringBuilder.append("Syntaktische Validierung: ");
    switch (validationHandling) {
      case KEEP_INVALID_ENTRIES:
        stringBuilder.append(
            "Werte, die nicht konform zu den im MDR hinterlegten Validierungsinformationen sind, "
                + "sind in diesem Dokument orange hinterlegt.");
        break;
      case REMOVE_INVALID_ENTRIES:
        stringBuilder.append(
            "Werte, die nicht konform zu den im MDR hinterlegten Validierungsinformationen sind,"
                + " wurden aus diesem Dokument entfernt.");
        break;
      case NO_VALIDATION:
      default:
        stringBuilder.append(
            "Es fand keine Validierung der Werte statt. Dieses Dokument kann deshalb Einträge "
                + "enthalten, die nicht konform zu den im MDR hinterlegten "
                + "Validierungsinformationen sind.");
        break;
    }

    stringBuilder.append("\n\nVor Verwendung der Datei beachten Sie unbedingt:\n");
    stringBuilder.append(
        "1. Bitte stellen Sie sicher, dass organisatorische Rahmenbedingungen für den Export "
            + "gegeben sind (insb. Zustimmung der Datenbesitzer und des Datenschutzes).\n");
    stringBuilder.append(
        "2. Es handelt sich um ein experimentelles, noch in der Erprobung befindliches Feature. "
            + "Technische Fehler sind nicht ausgeschlossen. Im Zweifel konsultieren Sie die "
            + "Quelldatenbank.\n");
    stringBuilder.append(
        "3. Die exportierten Daten haben eine sehr hohe Präzision. Es wurden keine Maßnahmen zur "
            + "Anonymisierung vorgenommen. Bitte prüfen Sie die Ausgabe und anonymisieren Sie die "
            + "Daten ggfls. selbst, um eine unerlaubte Reidentifikation von Patienten/Probanden "
            + "auszuschließen.\n");
    stringBuilder.append(
        "\nBei Fragen wenden Sie sich bitte an Ihren Standortvertreter der Arbeitsgruppe CCP-IT.");

    Cell contentCell = contentRow.createCell(0);
    contentCell.setCellValue(stringBuilder.toString());
    contentCell.setCellStyle(cs);

    // Create a border around the two cells (header and content)
    CellRangeAddress region = new CellRangeAddress(0, 1, 0, 0);
    RegionUtil.setBorderBottom(BorderStyle.THICK, region, infoSheet);
    RegionUtil.setBorderTop(BorderStyle.THICK, region, infoSheet);
    RegionUtil.setBorderLeft(BorderStyle.THICK, region, infoSheet);
    RegionUtil.setBorderRight(BorderStyle.THICK, region, infoSheet);
  }

  private String getCurrentTimestamp (){
    //TODO
    return null;
  }


   */
}
