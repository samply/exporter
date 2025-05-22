package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateProjectBody {

    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "title")
    private String title;
    @JsonProperty(value = "description")
    private String description;
    @JsonProperty(value = "database")
    private String database;
    @JsonProperty(value = "vcfStoreService")
    private String vcfStoreService;
    @JsonProperty(value = "exportFolder")
    private String exportFolder;
}
