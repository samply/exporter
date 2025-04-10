package de.samply.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;
import java.util.UUID;

public class ContainerTemplate {


    @JsonIgnore
    private UUID uuid = UUID.randomUUID();

    @JacksonXmlProperty(isAttribute = true, localName = "id")
    @JsonProperty("id")
    private String id;

    @JacksonXmlProperty(isAttribute = true, localName = "default-name")
    @JsonProperty("default-name")
    private String defaultName;

    @JacksonXmlProperty(isAttribute = true, localName = "csv-filename")
    @JsonProperty("csv-filename")
    private String csvFilename;
    @JacksonXmlProperty(isAttribute = true, localName = "json-filename")
    @JsonProperty("json-filename")
    private String jsonFilename;
    @JacksonXmlProperty(isAttribute = true, localName = "json-key")
    @JsonProperty("json-key")
    private String jsonKey;
    @JacksonXmlProperty(isAttribute = true, localName = "xml-filename")
    @JsonProperty("xml-filename")
    private String xmlFilename;
    @JacksonXmlProperty(isAttribute = true, localName = "xml-root-element")
    @JsonProperty("xml-root-element")
    private String xmlRootElement;
    @JacksonXmlProperty(isAttribute = true, localName = "xml-element")
    @JsonProperty("xml-element")
    private String xmlElement;
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

    public String getCsvFilename() {
        return csvFilename;
    }

    public void setCsvFilename(String csvFilename) {
        this.csvFilename = csvFilename;
    }

    public String getExcelSheet() {
        return getNameOrDefault(excelSheet);
    }

    public void setExcelSheet(String excelSheet) {
        this.excelSheet = excelSheet;
    }

    public String getOpalTable() {
        return getNameOrDefault(opalTable);
    }

    public void setOpalTable(String opalTable) {
        this.opalTable = opalTable;
    }

    public String getOpalEntityType() {
        return getNameOrDefault(opalEntityType);
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
        return getNameOrDefaultWithSuffix(jsonFilename, "-${TIMESTAMP}.json");
    }

    public void setJsonFilename(String jsonFilename) {
        this.jsonFilename = jsonFilename;
    }

    public String getJsonKey() {
        return getNameOrDefault(jsonKey);
    }

    public void setJsonKey(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String getXmlFilename() {
        return getNameOrDefaultWithSuffix(xmlFilename, "-${TIMESTAMP}.xml");
    }

    public void setXmlFilename(String xmlFilename) {
        this.xmlFilename = xmlFilename;
    }

    public String getXmlElement() {
        return getNameOrDefault(xmlElement);
    }

    public void setXmlElement(String xmlElement) {
        this.xmlElement = xmlElement;
    }

    public String getXmlRootElement() {
        return getNameOrDefaultWithSuffix(xmlRootElement, "-root");
    }

    public void setXmlRootElement(String xmlRootElement) {
        this.xmlRootElement = xmlRootElement;
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

    @JsonIgnore
    private String getNameOrDefault(String name) {
        return (name != null) ? name : (defaultName != null) ? defaultName : uuid.toString();
    }

    @JsonIgnore
    private String getNameOrDefaultWithSuffix(String name, String suffix) {
        return (name != null) ? name : ((defaultName != null) ? defaultName : uuid.toString()) + ((suffix != null) ? suffix : "");
    }

}
