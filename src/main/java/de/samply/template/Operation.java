package de.samply.template;

import java.util.function.Function;

public enum Operation {
  // "http://host/fhir/Patient/123/_history/1" or "http://host/fhir/Patient/123" -> "Patient/123".
  // Values that are already relative ("Patient/123") or a bare id ("123") are returned unchanged.
  EXTRACT_RELATIVE_ID(value -> {
    int historyIndex = value.indexOf("/_history");
    String withoutHistory = (historyIndex >= 0) ? value.substring(0, historyIndex) : value;
    int idSlash = withoutHistory.lastIndexOf("/");
    if (idSlash < 0) {
      return withoutHistory;
    }
    int begin = withoutHistory.substring(0, idSlash).lastIndexOf("/") + 1;
    return withoutHistory.substring(begin);
  });

  private Function<String, String> function;

  Operation(Function<String, String> function) {
    this.function = function;
  }

  public String execute(String value) {
    return (value != null && value.length() > 0) ? this.function.apply(value) : value;
  }

}
