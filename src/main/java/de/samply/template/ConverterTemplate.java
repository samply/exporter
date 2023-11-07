package de.samply.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import de.samply.exporter.ExporterConst;
import de.samply.opal.model.Permission;
import de.samply.opal.model.PermissionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JacksonXmlRootElement(localName = "template")
public class ConverterTemplate {

    @JsonIgnore
    private UUID uuid = UUID.randomUUID();

    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty("id")
    private String id;

    @JacksonXmlProperty(isAttribute = true, localName = "default-name")
    @JsonProperty("default-name")
    private String defaultName;

    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty("ignore")
    private Boolean ignore;

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

    @JacksonXmlProperty(isAttribute = true, localName = "cql")
    @JsonProperty(value = "cql")
    private CqlTemplate cqlTemplate;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("container")
    private List<ContainerTemplate> containerTemplates = new ArrayList<>();

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("fhir-rev-include")
    private List<String> fhirRevIncludes = new ArrayList<>();

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("fhir-package")
    private List<String> fhirPackages = new ArrayList<>();

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("fhir-terminology-server")
    private List<String> fhirTerminologyServers = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getIgnore() {
        return (ignore != null) ? ignore : false;
    }

    public void setIgnore(Boolean ignore) {
        this.ignore = ignore;
    }

    public String getExcelFilename() {
        return getNameOrDefaultWithSuffix(excelFilename, "-${TIMESTAMP}.xlsx");
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

    public List<String> getFhirPackages() {
        return fhirPackages;
    }

    public void setFhirPackages(List<String> fhirPackages) {
        this.fhirPackages = fhirPackages;
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
        return getNameOrDefaultWithSuffix(opalProject, "-${TIMESTAMP}");
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

    public List<String> getFhirTerminologyServers() {
        return fhirTerminologyServers;
    }

    public void setFhirTerminologyServers(List<String> fhirTerminologyServers) {
        this.fhirTerminologyServers = fhirTerminologyServers;
    }

    public CqlTemplate getCqlTemplate() {
        return cqlTemplate;
    }

    public void setCqlTemplate(CqlTemplate cqlTemplate) {
        this.cqlTemplate = cqlTemplate;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    @JsonIgnore
    public Optional<AttributeTemplate> fetchLinkedAttributeTemplate(AttributeTemplate attributeTemplate) {
        Optional<ContainerIdAttributeId> containerIdAttributeId = fetchContainerIdAttributeId(attributeTemplate);
        if (containerIdAttributeId.isPresent()) {
            Optional<ContainerTemplate> containerTemplate = fetchContainerTemplate(containerIdAttributeId.get().containerId);
            if (containerTemplate.isPresent()) {
                return fetchAttributeTemplate(containerTemplate.get(), containerIdAttributeId.get().attributeId);
            }
        }
        return Optional.empty();
    }

    @JsonIgnore
    private static Optional<ContainerIdAttributeId> fetchContainerIdAttributeId(AttributeTemplate attributeTemplate) {
        if (attributeTemplate != null && attributeTemplate.getLink() != null) {
            String[] split = attributeTemplate.getLink().trim().split("\\.");
            if (split.length >= 2 && split[0].trim().length() > 0 && split[1].trim().length() > 0) {
                return Optional.of(new ContainerIdAttributeId(split[0], split[1]));
            }
        }
        return Optional.empty();
    }

    @JsonIgnore
    private Optional<ContainerTemplate> fetchContainerTemplate(String containerId) {
        return containerTemplates.stream().filter(containerTemplate ->
                containerTemplate.getId() != null && containerTemplate.getId().equalsIgnoreCase(containerId)).findFirst();
    }

    @JsonIgnore
    private Optional<AttributeTemplate> fetchAttributeTemplate(ContainerTemplate containerTemplate, String attributeId) {
        return containerTemplate.getAttributeTemplates().stream().filter(attributeTemplate ->
                attributeTemplate.getId() != null && attributeTemplate.getId().equalsIgnoreCase(attributeId)).findFirst();
    }

    private record ContainerIdAttributeId(String containerId, String attributeId) {
    }

    @JsonIgnore
    private String getNameOrDefaultWithSuffix(String name, String suffix) {
        return (name != null) ? name : ((defaultName != null) ? defaultName : uuid.toString()) + ((suffix != null) ? suffix : "");
    }


}
