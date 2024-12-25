package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CsvDatasourceTable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("data")
    private String data;

    @JsonProperty("entityType")
    private String entityType;

    @JsonProperty("refTable")
    private String refTable;
}
