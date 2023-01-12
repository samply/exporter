package de.samply.converter;

import de.samply.container.Containers;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hl7.fhir.r4.model.Bundle;

public enum Format {

  FHIR_QUERY (String.class, true),
  CQL_QUERY (String.class, true),
  BUNDLE (Bundle.class, false),
  CONTAINERS (Containers.class, false),
  CSV (Path.class, false),
  EXCEL (Path.class, false);

  private Class zClass;
  private boolean isQuery;

  Format(Class zClass, boolean isQuery) {
    this.zClass = zClass;
    this.isQuery = isQuery;
  }

  public boolean isInstance(Object object){
    return zClass.isInstance(object);
  }

  public static boolean isExistentQueryFormat(String queryFormat){
    AtomicBoolean result = new AtomicBoolean(false);
    Arrays.stream(values()).forEach(format -> {
      if ( format.isQuery && format.toString().equals(queryFormat)){
        result.set(true);
      }
    });
    return result.get();
  }

  public boolean isQuery() {
    return isQuery;
  }

  public boolean isPath(){
    return zClass == Path.class;
  }

}
