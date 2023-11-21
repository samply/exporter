package de.samply.opal;

import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Path;

public class Session {

  private ConverterTemplate converterTemplate;
  private ContainerTemplateMatcher containerTemplateMatcher;
  private ConverterTemplateUtils converterTemplateUtils;
  private int timeoutInSeconds;
  private int maxNumberOfRetries;
  private String project;
  private HttpServletRequest httpServletRequest;

  public Session(ConverterTemplate template, ConverterTemplateUtils converterTemplateUtils,
      int timeoutInSeconds, int maxNumberOfRetries, HttpServletRequest httpServletRequest) {
    this.converterTemplate = template;
    this.containerTemplateMatcher = new ContainerTemplateMatcher(template);
    this.converterTemplateUtils = converterTemplateUtils;
    this.timeoutInSeconds = timeoutInSeconds;
    this.maxNumberOfRetries = maxNumberOfRetries;
    this.httpServletRequest = httpServletRequest;
  }

  public ContainerTemplate getContainerTemplate(String csvFilename) {
    return containerTemplateMatcher.getContainerTemplate(csvFilename);
  }

  public ConverterTemplate getConverterTemplate() {
    return converterTemplate;
  }

  public String fetchProject() {
    if (project == null) {
      project = converterTemplate.getOpalProject();
      if (converterTemplateUtils != null) {
        project = converterTemplateUtils.replaceTokens(project, httpServletRequest);
      }
    }
    return project;
  }

  public String fetchOpalProjectDirectory(String opalDirectory) {
    return opalDirectory + '/' + fetchProject();
  }

  public String fetchOpalProjectDirectoryPath(String opalDirectory, Path path) {
    return opalDirectory + '/' + fetchProject() + '/' + path.getFileName().toString();
  }

  public int getTimeoutInSeconds() {
    return timeoutInSeconds;
  }

  public void setTimeoutInSeconds(int timeoutInSeconds) {
    this.timeoutInSeconds = timeoutInSeconds;
  }

  public int getMaxNumberOfRetries() {
    return maxNumberOfRetries;
  }

  public void setMaxNumberOfRetries(int maxNumberOfRetries) {
    this.maxNumberOfRetries = maxNumberOfRetries;
  }

}
