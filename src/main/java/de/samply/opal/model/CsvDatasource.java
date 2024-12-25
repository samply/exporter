package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CsvDatasource {

    @JsonProperty("Magma.CsvDatasourceFactoryDto.params")
    private Params params;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Params {
        @JsonProperty("characterSet")
        private String characterSet;

        @JsonProperty("firstRow")
        private int firstRow;

        @JsonProperty("quote")
        private String quote;

        @JsonProperty("separator")
        private String separator;

        @JsonProperty("defaultValueType")
        private String defaultValueType;

        @JsonProperty("tables")
        private List<CsvDatasourceTable> tables;
    }
}