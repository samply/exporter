package de.samply.fhir;

import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import org.hl7.fhir.r4.model.Resource;

public record ResourceAttribute (
    Resource idResource,
    Resource valueResource,
    String attributeValue,
    ContainerTemplate containerTemplate,
    AttributeTemplate attributeTemplate
) {

  public String fetchContainerId(){
    return idResource.getIdPart();
  }
}
