package de.samply.xml;

import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplateUtils;

public class Session extends de.samply.files.Session {

  public Session(ConverterTemplateUtils converterTemplateUtils,
      String writeDirectory) {
    super(converterTemplateUtils, writeDirectory);
  }

  @Override
  protected String getFilename(ContainerTemplate containerTemplate) {
    return containerTemplate.getXmlFilename();
  }

}
