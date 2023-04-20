package de.samply.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

public class ContainerTemplate {

  @JacksonXmlProperty(isAttribute = true, localName = "csv-filename")
  @JsonProperty("csv-filename")
  private String csvFilename;
  @JacksonXmlProperty(isAttribute = true, localName = "json-filename")
  @JsonProperty("json-filename")
  private String jsonFilename;

  @JacksonXmlProperty(isAttribute = true, localName = "json-key")
  @JsonProperty("json-key")
  private String jsonKey;

  @JacksonXmlProperty(isAttribute = true, localName = "excel-sheet")
  @JsonProperty("excel-sheet")
  private String excelSheet;
  @JacksonXmlProperty(isAttribute = true, localName = "opal-table")
  @JsonProperty("opal-table")
  private String opalTable;
  @JacksonXmlProperty(isAttribute = true, localName = "opal-entity-type")
  @JsonProperty("opal-entity-type")
  private String opalEntityType;
  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("attribute")
  private List<AttributeTemplate> attributeTemplates;

  public ContainerTemplate() {
  }

  public ContainerTemplate(String csvFilename, String excelSheet,
      List<AttributeTemplate> attributeTemplates) {
    this.csvFilename = csvFilename;
    this.excelSheet = excelSheet;
    this.attributeTemplates = attributeTemplates;
  }

  public String getCsvFilename() {
    return csvFilename;
  }

  public void setCsvFilename(String csvFilename) {
    this.csvFilename = csvFilename;
  }

  public String getExcelSheet() {
    return excelSheet;
  }

  public void setExcelSheet(String excelSheet) {
    this.excelSheet = excelSheet;
  }

  public String getOpalTable() {
    return opalTable;
  }

  public void setOpalTable(String opalTable) {
    this.opalTable = opalTable;
  }

  public String getOpalEntityType() {
    return opalEntityType;
  }

  public void setOpalEntityType(String opalEntityType) {
    this.opalEntityType = opalEntityType;
  }

  public List<AttributeTemplate> getAttributeTemplates() {
    return attributeTemplates;
  }

  public void setAttributeTemplates(
      List<AttributeTemplate> attributeTemplates) {
    this.attributeTemplates = attributeTemplates;
  }

  public String getJsonFilename() {
    return jsonFilename;
  }

  public void setJsonFilename(String jsonFilename) {
    this.jsonFilename = jsonFilename;
  }

  public String getJsonKey() {
    return jsonKey;
  }

  public void setJsonKey(String jsonKey) {
    this.jsonKey = jsonKey;
  }

}
