package de.samply.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import de.samply.teiler.TeilerConst;
import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "template")
public class ConverterTemplate {

  @JacksonXmlProperty(isAttribute = true)
  @JsonProperty("id")
  private String id;
  @JacksonXmlProperty(isAttribute = true, localName = "excel-filename")
  @JsonProperty("excel-filename")
  private String excelFilename;

  @JacksonXmlProperty(isAttribute = true, localName = "csv-separator")
  @JsonProperty(value = "csv-separator")
  private String csvSeparator = TeilerConst.DEFAULT_CSV_SEPARATOR;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("container")
  private List<ContainerTemplate> containerTemplates = new ArrayList<>();

  @JacksonXmlElementWrapper(useWrapping = false) @JsonProperty("fhir-rev-include")
  private List<String> fhirRevIncludes = new ArrayList<>();

  public ConverterTemplate() {
  }

  public ConverterTemplate(String id, String excelFilename,
      List<ContainerTemplate> containerTemplates) {
    this.id = id;
    this.excelFilename = excelFilename;
    this.containerTemplates = containerTemplates;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getExcelFilename() {
    return excelFilename;
  }

  public String replaceTokenAndGetExcelFilename(){
    return ConverterTemplateUtils.replaceTokens(excelFilename);
  }

  public void setExcelFilename(String excelFilename) {
    this.excelFilename = excelFilename;
  }

  public String getCsvSeparator() {
    return csvSeparator;
  }

  public void setCsvSeparator(String csvSeparator) {
    this.csvSeparator = csvSeparator;
  }

  public List<ContainerTemplate> getContainerTemplates() {
    return containerTemplates;
  }

  public void setContainerTemplates(
      List<ContainerTemplate> containerTemplates) {
    this.containerTemplates = containerTemplates;
  }

  public List<String> getFhirRevIncludes() {
    return fhirRevIncludes;
  }

  public void setFhirRevIncludes(List<String> fhirRevIncludes) {
    this.fhirRevIncludes = fhirRevIncludes;
  }

}
