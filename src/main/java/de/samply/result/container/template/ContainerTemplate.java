package de.samply.result.container.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

public class ContainerTemplate {

  @JacksonXmlProperty(isAttribute = true)@JsonProperty("order")
  private Integer order;
  @JacksonXmlProperty(isAttribute = true, localName = "csv-filename")@JsonProperty("csv-filename")
  private String csvFilename;
  @JacksonXmlProperty(isAttribute = true, localName = "excel-sheet")@JsonProperty("excel-sheet")
  private String excelSheet;
  @JacksonXmlElementWrapper(useWrapping = false) @JsonProperty("attribute")
  private List<AttributeTemplate> attributeTemplates;

  public ContainerTemplate() {
  }

  public ContainerTemplate(Integer order, String csvFilename, String excelSheet,
      List<AttributeTemplate> attributeTemplates) {
    this.order = order;
    this.csvFilename = csvFilename;
    this.excelSheet = excelSheet;
    this.attributeTemplates = attributeTemplates;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = order;
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

  public List<AttributeTemplate> getAttributeTemplates() {
    return attributeTemplates;
  }

  public void setAttributeTemplates(
      List<AttributeTemplate> attributeTemplates) {
    this.attributeTemplates = attributeTemplates;
  }

}
