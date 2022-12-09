package de.samply.blaze;

import de.samply.container.template.AttributeTemplate;
import de.samply.container.template.ContainerTemplate;
import org.hl7.fhir.r4.model.Resource;

public record ResourceAttribute (
    Resource resource,
    String attributeValue,
    ContainerTemplate containerTemplate,
    AttributeTemplate attributeTemplate
) {

  public String fetchContainerId(){
    //TODO
    return resource.getIdPart();
  }
}
