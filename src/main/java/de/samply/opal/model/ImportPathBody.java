package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportPathBody {

    @JsonProperty(value = "destination")
    private String destination;
    @JsonProperty(value = "tables")
    private String[] tables;
}
