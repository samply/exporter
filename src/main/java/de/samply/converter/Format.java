package de.samply.converter;

import de.samply.container.Containers;
import java.nio.file.Path;
import org.hl7.fhir.r4.model.Bundle;

public enum Format {

  FHIR_QUERY (String.class),
  CQL_QUERY (String.class),
  BUNDLE (Bundle.class),
  CONTAINERS (Containers.class),
  CSV (Path.class),
  EXCEL (Path.class);

  private Class zClass;

  Format(Class zClass) {
    this.zClass = zClass;
  }

  public boolean isInstance(Object object){
    return zClass.isInstance(object);
  }

}
