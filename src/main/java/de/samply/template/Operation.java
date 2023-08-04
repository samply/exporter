package de.samply.template;

import java.util.function.Function;

public enum Operation {
  EXTRACT_RELATIVE_ID(value -> {
    int end = value.indexOf("/_history");
    int tempIndex = value.substring(0, end).lastIndexOf("/");
    int begin = value.substring(0, tempIndex).lastIndexOf("/") + 1;
    return value.substring(begin, end);
  });

  private Function<String, String> function;

  Operation(Function<String, String> function) {
    this.function = function;
  }

  public String execute(String value) {
    return (value != null && value.length() > 0) ? this.function.apply(value) : value;
  }

}
