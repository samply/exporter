package de.samply.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.samply.exporter.ExporterConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AttributeTemplate {

    @JsonIgnore
    private UUID uuid = UUID.randomUUID();

    @JacksonXmlProperty(isAttribute = true, localName = "id")
    @JsonProperty("id")
    private String id;

    @JacksonXmlProperty(isAttribute = true, localName = "default-name")
    @JsonProperty("default-name")
    private String defaultName;

    @JacksonXmlProperty(isAttribute = true, localName = "link")
    @JsonProperty("link")
    private String link;

    @JacksonXmlProperty(isAttribute = true, localName = "csv-column")
    @JsonProperty("csv-column")
    private String csvColumnName;

    @JacksonXmlProperty(isAttribute = true, localName = "excel-column")
    @JsonProperty("excel-column")
    private String excelColumnName;

    @JacksonXmlProperty(isAttribute = true, localName = "json-key")
    @JsonProperty("json-key")
    private String jsonKey;

    @JacksonXmlProperty(isAttribute = true, localName = "xml-element")
    @JsonProperty("xml-element")
    private String xmlElement;

    @JacksonXmlProperty(isAttribute = true, localName = "opal-value-type")
    @JsonProperty("opal-value-type")
    private String opalValueType;

    @JacksonXmlProperty(isAttribute = true, localName = "opal-script")
    @JsonProperty("opal-script")
    private String opalScript;

    @JacksonXmlProperty(isAttribute = true, localName = "primary-key")
    @JsonProperty("primary-key")
    private boolean primaryKey = false;

    @JacksonXmlProperty(isAttribute = true, localName = "validation")
    @JsonProperty("validation")
    private boolean validation = false;

    @JacksonXmlProperty(isAttribute = true, localName = "val-fhir-path")
    @JsonProperty("val-fhir-path")
    private String valFhirPath;

    @JacksonXmlProperty(isAttribute = true, localName = "join-fhir-path")
    @JsonProperty("join-fhir-path")
    private String joinFhirPath;

    @JacksonXmlProperty(isAttribute = true, localName = "condition-value-fhir-path")
    @JsonProperty("condition-value-fhir-path")
    private String conditionValueFhirPath;

    @JacksonXmlProperty(isAttribute = true, localName = "condition-id-fhir-path")
    @JsonProperty("condition-id-fhir-path")
    private String conditionIdFhirPath;

    @JacksonXmlProperty(isAttribute = true, localName = "anonym")
    @JsonProperty("anonym")
    private String anonym;

    @JacksonXmlProperty(isAttribute = true, localName = "mdr")
    @JsonProperty("mdr")
    private String mdr;

    @JacksonXmlProperty(isAttribute = true, localName = "op")
    @JsonProperty("op")
    private Operation operation;

    public String getCsvColumnName() {
        return getNameOrDefault(csvColumnName);
    }

    public void setCsvColumnName(String csvColumnName) {
        this.csvColumnName = csvColumnName;
    }

    public String getExcelColumnName() {
        return getNameOrDefault(excelColumnName);
    }

    public void setExcelColumnName(String excelColumnName) {
        this.excelColumnName = excelColumnName;
    }

    public String getOpalValueType() {
        return opalValueType;
    }

    public void setOpalValueType(String opalValueType) {
        this.opalValueType = opalValueType;
    }

    public String getValFhirPath() {
        return valFhirPath;
    }

    public void setValFhirPath(String valFhirPath) {
        this.valFhirPath = valFhirPath;
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

    public String getConditionValueFhirPath() {
        return conditionValueFhirPath;
    }

    public void setConditionValueFhirPath(String conditionValueFhirPath) {
        this.conditionValueFhirPath = conditionValueFhirPath;
    }

    public String getJoinFhirPath() {
        return joinFhirPath;
    }

    public void setJoinFhirPath(String joinFhirPath) {
        this.joinFhirPath = joinFhirPath;
    }

    public List<String> fetchJoinFhirPaths() {
        return (joinFhirPath == null) ? new ArrayList<>() :
                Arrays.asList(joinFhirPath.trim().split(ExporterConst.RELATED_FHIR_PATH_DELIMITER));
    }

    @JsonIgnore
    private Boolean isDirectJoinFhirPath;

    @JsonIgnore
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

    @JsonIgnore
    private Boolean isChildFhirPath = null;

    @JsonIgnore
    public boolean isDirectParentFhirPath() {
        if (joinFhirPath == null) {
            return false;
        }
        if (isChildFhirPath == null) {
            isChildFhirPath = isChildFhirPath(joinFhirPath);
        }
        return !isChildFhirPath && isDirectJoinFhirPath();
    }

    @JsonIgnore
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
        return joinFhirPath.startsWith(ExporterConst.CHILD_FHIR_PATH_HEAD);
    }

    public static String removeChildFhirPathHead(String joinFhirPath) {
        return (joinFhirPath.startsWith(ExporterConst.CHILD_FHIR_PATH_HEAD)) ? joinFhirPath.substring(1)
                : joinFhirPath;
    }

    public String getConditionIdFhirPath() {
        return conditionIdFhirPath;
    }

    public void setConditionIdFhirPath(String conditionIdFhirPath) {
        this.conditionIdFhirPath = conditionIdFhirPath;
    }

    public String getOpalScript() {
        return opalScript;
    }

    public void setOpalScript(String opalScript) {
        this.opalScript = opalScript;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isValidation() {
        return validation;
    }

    public void setValidation(boolean validation) {
        this.validation = validation;
    }

    public String getJsonKey() {
        return getNameOrDefault(jsonKey);
    }

    public void setJsonKey(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String getXmlElement() {
        return getNameOrDefault(xmlElement);
    }

    public void setXmlElement(String xmlElement) {
        this.xmlElement = xmlElement;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @JsonIgnore
    private String getNameOrDefault(String name) {
        return (name != null) ? name : (defaultName != null) ? defaultName : uuid.toString();
    }

}
