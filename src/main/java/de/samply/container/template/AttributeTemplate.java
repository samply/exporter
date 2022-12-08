package de.samply.container.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class AttributeTemplate {

  @JacksonXmlProperty(isAttribute = true)
  @JsonProperty("order")
  private Integer order;
  @JacksonXmlProperty(isAttribute = true, localName = "csv-column")
  @JsonProperty("csv-column")
  private String csvColumnName;
  @JacksonXmlProperty(isAttribute = true, localName = "excel-column")
  @JsonProperty("excel-column")
  private String excelColumnName;
  @JacksonXmlProperty(isAttribute = true, localName = "fhir-path")
  @JsonProperty("fhir-path")
  private String fhirPath;

  public AttributeTemplate() {
  }

  public AttributeTemplate(Integer order, String csvColumnName, String excelColumnName,
      String fhirPath) {
    this.order = order;
    this.csvColumnName = csvColumnName;
    this.excelColumnName = excelColumnName;
    this.fhirPath = fhirPath;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = order;
  }

  public String getCsvColumnName() {
    return csvColumnName;
  }

  public void setCsvColumnName(String csvColumnName) {
    this.csvColumnName = csvColumnName;
  }

  public String getExcelColumnName() {
    return excelColumnName;
  }

  public void setExcelColumnName(String excelColumnName) {
    this.excelColumnName = excelColumnName;
  }

  public String getFhirPath() {
    return fhirPath;
  }

  public void setFhirPath(String fhirPath) {
    this.fhirPath = fhirPath;
  }

  public String fetchRootFhirPath() {
    String rootFhirPath = fhirPath;
    if (fhirPath != null) {
      int index = fhirPath.indexOf('.');
      if (index > 0) {
        String firstElement = fhirPath.substring(0, index);
        rootFhirPath =
            "Bundle.entry.select(resource as " + firstElement + ")" + fhirPath.substring(index);
      }
    }
    return rootFhirPath;
  }

}
