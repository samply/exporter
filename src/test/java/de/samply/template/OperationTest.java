package de.samply.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OperationTest {

  @Test
  void extractRelativeId() {
    Operation op = Operation.EXTRACT_RELATIVE_ID;
    assertEquals("Patient/123", op.execute("http://blaze:8080/fhir/Patient/123/_history/5"));
    assertEquals("Patient/123", op.execute("http://blaze:8080/fhir/Patient/123"));
    assertEquals("Patient/123", op.execute("Patient/123"));
    assertEquals("123", op.execute("123"));
    assertEquals("", op.execute(""));
  }
}
