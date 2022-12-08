package de.samply.container;

public class Attribute {

  private String id;
  // If isContainerRef is true, then type is the container-type.
  private String type;
  private String value;
  private boolean isContainerRef = false;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public boolean isContainerRef() {
    return isContainerRef;
  }

  public void setContainerRef() {
    isContainerRef = true;
  }

}
