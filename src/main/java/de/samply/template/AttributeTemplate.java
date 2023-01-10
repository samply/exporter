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

  @JacksonXmlProperty(isAttribute = true, localName = "join-fhir-path")
  @JsonProperty("join-fhir-path")
  private String joinFhirPath;

  @JacksonXmlProperty(isAttribute = true, localName = "condition-fhir-path")
  @JsonProperty("condition-fhir-path")
  private String conditionFhirPath;

  @JacksonXmlProperty(isAttribute = true, localName = "anonym")
  @JsonProperty("anonym")
  private String anonym;

  @JacksonXmlProperty(isAttribute = true, localName = "mdr")
  @JsonProperty("mdr")
  private String mdr;

  @JacksonXmlProperty(isAttribute = true, localName = "op")
  @JsonProperty("op")
  private Operation operation;

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

  public Operation getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    if (operation != null) {
      this.operation = Operation.valueOf(operation);
    }
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

  public String getConditionFhirPath() {
    return conditionFhirPath;
  }

  public void setConditionFhirPath(String conditionFhirPath) {
    this.conditionFhirPath = conditionFhirPath;
  }

  public String getJoinFhirPath() {
    return joinFhirPath;
  }

  public void setJoinFhirPath(String joinFhirPath) {
    this.joinFhirPath = joinFhirPath;
  }

  public List<String> fetchJoinFhirPaths() {
    return (joinFhirPath == null) ? new ArrayList<>() :
        Arrays.asList(joinFhirPath.trim().split(TeilerConst.RELATED_FHIR_PATH_DELIMITER));
  }

  private Boolean isDirectJoinFhirPath;

  public boolean isDirectJoinFhirPath() {
    if (joinFhirPath == null) {
      return false;
    }
    if (isDirectJoinFhirPath == null) {
      isDirectJoinFhirPath = true;
      Boolean isParentFhirPath = null;
      for (String joinFhirPath : fetchJoinFhirPaths()) {
        boolean isChildFhirPath = isChildFhirPath(joinFhirPath);
        if (isParentFhirPath == null) {
          isParentFhirPath = !isChildFhirPath;
        } else {
          if (isParentFhirPath && isChildFhirPath) {
            isDirectJoinFhirPath = false;
            break;
          }
        }
      }
    }
    return isDirectJoinFhirPath;
  }

  private Boolean isChildFhirPath = null;

  public boolean isDirectParentFhirPath() {
    if (joinFhirPath == null){
      return false;
    }
    if (isChildFhirPath == null) {
      isChildFhirPath = isChildFhirPath(joinFhirPath);
    }
    return !isChildFhirPath && isDirectJoinFhirPath();
  }

  public boolean isDirectChildFhirPath() {
    if (joinFhirPath == null) {
      return false;
    }
    if (isChildFhirPath == null) {
      isChildFhirPath = isChildFhirPath(joinFhirPath);
    }
    return isChildFhirPath && isDirectJoinFhirPath();
  }

  public static boolean isChildFhirPath(String joinFhirPath) {
    return joinFhirPath.startsWith(TeilerConst.CHILD_FHIR_PATH_HEAD);
  }

  public static String removeChildFhirPathHead(String joinFhirPath) {
    return (joinFhirPath.startsWith(TeilerConst.CHILD_FHIR_PATH_HEAD)) ? joinFhirPath.substring(1)
        : joinFhirPath;
  }



}
