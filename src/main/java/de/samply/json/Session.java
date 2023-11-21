package de.samply.json;

import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplateUtils;
import jakarta.servlet.http.HttpServletRequest;

public class Session extends de.samply.files.Session {

  public Session(ConverterTemplateUtils converterTemplateUtils, String writeDirectory, HttpServletRequest httpServletRequest) {
    super(converterTemplateUtils, writeDirectory, httpServletRequest);
  }

  @Override
  protected String getFilename(ContainerTemplate containerTemplate) {
    return containerTemplate.getJsonFilename();
  }

}
