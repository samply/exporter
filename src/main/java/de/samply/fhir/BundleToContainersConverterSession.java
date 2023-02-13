package de.samply.fhir;

import de.samply.exporter.ExporterConst;
import de.samply.template.AttributeTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BundleToContainersConverterSession {


  private Map<String, AtomicInteger> idNexAnonymMap = new HashMap<>();

  public String generateAnonym(AttributeTemplate attributeTemplate) {
    String type = attributeTemplate.getAnonym();
    if (type == null) {
      type = ExporterConst.DEFAULT_ANONYM_ELEMENT;
    }
    return type + "_" + fetchNextAnonym(attributeTemplate);
  }

  private Integer fetchNextAnonym(AttributeTemplate attributeTemplate) {
    AtomicInteger nextAnonym = idNexAnonymMap.get(attributeTemplate.getAnonym());
    if (nextAnonym == null) {
      nextAnonym = new AtomicInteger(ExporterConst.FIRST_ANONYM_ID);
      idNexAnonymMap.put(attributeTemplate.getAnonym(), nextAnonym);
    }
    return nextAnonym.getAndIncrement();
  }


}
