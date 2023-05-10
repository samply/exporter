package de.samply.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import de.samply.exporter.ExporterConst;
import de.samply.opal.model.Permission;
import de.samply.opal.model.PermissionType;
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
  private String csvSeparator = ExporterConst.DEFAULT_CSV_SEPARATOR;

  @JacksonXmlProperty(isAttribute = true, localName = "source-id")
  @JsonProperty(value = "source-id")
  private String sourceId;

  @JacksonXmlProperty(isAttribute = true, localName = "target-id")
  @JsonProperty(value = "target-id")
  private String targetId;

  @JacksonXmlProperty(isAttribute = true, localName = "opal-project")
  @JsonProperty(value = "opal-project")
  private String opalProject;

  @JacksonXmlProperty(isAttribute = true, localName = "fhir-profile-url")
  @JsonProperty(value = "fhir-profile-url")
  private String fhirProfileUrl;

  @JacksonXmlProperty(isAttribute = true, localName = "opal-permission-type")
  @JsonProperty(value = "opal-permission-type")
  private PermissionType opalPermissionType;

  // Comma Separated if more than one
  @JacksonXmlProperty(isAttribute = true, localName = "opal-permission-subjects")
  @JsonProperty(value = "opal-permission-subjects")
  private String opalPermissionSubjects;

  @JacksonXmlProperty(isAttribute = true, localName = "opal-permission")
  @JsonProperty(value = "opal-permission")
  private Permission opalPermission;

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

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getTargetId() {
    return targetId;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }

  public String getOpalProject() {
    return opalProject;
  }

  public void setOpalProject(String opalProject) {
    this.opalProject = opalProject;
  }

  public PermissionType getOpalPermissionType() {
    return opalPermissionType;
  }

  public void setOpalPermissionType(PermissionType opalPermissionType) {
    this.opalPermissionType = opalPermissionType;
  }

  public String getOpalPermissionSubjects() {
    return opalPermissionSubjects;
  }

  public void setOpalPermissionSubjects(String opalPermissionSubjects) {
    this.opalPermissionSubjects = opalPermissionSubjects;
  }

  public Permission getOpalPermission() {
    return opalPermission;
  }

  public void setOpalPermission(Permission opalPermission) {
    this.opalPermission = opalPermission;
  }

  public String getFhirProfileUrl() {
    return fhirProfileUrl;
  }

  public void setFhirProfileUrl(String fhirProfileUrl) {
    this.fhirProfileUrl = fhirProfileUrl;
  }

}
