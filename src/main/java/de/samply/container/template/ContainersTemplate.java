package de.samply.container.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

@JacksonXmlRootElement(localName = "template")
public class ContainersTemplate{

  @JacksonXmlProperty(isAttribute = true)@JsonProperty("id")
  private String id;
  @JacksonXmlProperty(isAttribute = true, localName = "excel-filename")@JsonProperty("excel-filename")
  private String excelFilename;
  @JacksonXmlElementWrapper(useWrapping = false) @JsonProperty("container")
  private List<ContainerTemplate> containerTemplates;

  public ContainersTemplate() {
  }

  public ContainersTemplate(String id, String excelFilename,
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

  public List<ContainerTemplate> getContainerTemplates() {
    return containerTemplates;
  }

  public void setContainerTemplates(
      List<ContainerTemplate> containerTemplates) {
    this.containerTemplates = containerTemplates;
  }
}
