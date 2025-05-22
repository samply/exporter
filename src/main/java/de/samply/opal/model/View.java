package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class View {

  @JsonProperty(value = "from")
  private List<String> from = new ArrayList<>();

  @JsonProperty(value = "Magma.VariableListViewDto.view")
  private MagmaView magmaView;

  @JsonProperty(value = "name")
  private String name;


}
