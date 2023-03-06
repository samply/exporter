package de.samply.opal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class View {

  @JsonProperty(value = "from")
  private List<String> from = new ArrayList<>();

  @JsonProperty(value = "Magma.VariableListViewDto.view")
  private MagmaView magmaView;

  @JsonProperty(value = "name")
  private String name;


  public List<String> getFrom() {
    return from;
  }

  public void setFrom(List<String> from) {
    this.from = from;
  }

  public MagmaView getMagmaView() {
    return magmaView;
  }

  public void setMagmaView(MagmaView magmaView) {
    this.magmaView = magmaView;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
