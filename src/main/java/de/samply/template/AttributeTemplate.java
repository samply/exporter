package de.samply.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.samply.teiler.TeilerConst;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttributeTemplate {

  @JacksonXmlProperty(isAttribute = true, localName = "csv-column")
  @JsonProperty("csv-column")
  private String csvColumnName;
  @JacksonXmlProperty(isAttribute = true, localName = "excel-column")
  @JsonProperty("excel-column")
  private String excelColumnName;
  @JacksonXmlProperty(isAttribute = true, localName = "fhir-path")
  @JsonProperty("fhir-path")
  private String fhirPath;

  @JacksonXmlProperty(isAttribute = true, localName = "parent-fhir-path")
  @JsonProperty("parent-fhir-path")
  private String parentFhirPath;

  @JacksonXmlProperty(isAttribute = true, localName = "child-fhir-path")
  @JsonProperty("child-fhir-path")
  private String childFhirPath;

  @JacksonXmlProperty(isAttribute = true, localName = "anonym")
  @JsonProperty("anonym")
  private String anonym;

  @JacksonXmlProperty(isAttribute = true, localName = "mdr")
  @JsonProperty("mdr")
  private String mdr;


  public AttributeTemplate() {
  }

  public AttributeTemplate(String csvColumnName, String excelColumnName,
      String fhirPath) {
    this.csvColumnName = csvColumnName;
    this.excelColumnName = excelColumnName;
    this.fhirPath = fhirPath;
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

  public String getParentFhirPath() {
    return parentFhirPath;
  }

  public List<String> fetchParentFhirPaths() {
    return (parentFhirPath == null) ? new ArrayList<>() :
        Arrays.asList(parentFhirPath.trim().split(TeilerConst.RELATED_FHIR_PATH_DELIMITER));
  }

  public String getChildFhirPath() {
    return childFhirPath;
  }

  public List<String> fetchChildFhirPaths() {
    return (childFhirPath == null) ? new ArrayList<>() :
        Arrays.asList(childFhirPath.trim().split(TeilerConst.RELATED_FHIR_PATH_DELIMITER));
  }

  public void setChildFhirPath(String childFhirPath) {
    this.childFhirPath = childFhirPath;
  }

  public void setParentFhirPath(String parentFhirPath) {
    this.parentFhirPath = parentFhirPath;
  }

  public String getMdr() {
    return mdr;
  }

  public void setMdr(String mdr) {
    this.mdr = mdr;
  }

  public String getAnonym() {
    return anonym;
  }

  public void setAnonym(String anonym) {
    this.anonym = anonym;
  }

}
