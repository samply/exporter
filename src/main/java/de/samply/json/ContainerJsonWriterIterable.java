package de.samply.json;

import de.samply.container.Container;
import de.samply.files.ContainerFileWriterIterable;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContainerJsonWriterIterable extends ContainerFileWriterIterable {

  private boolean isFirstElement = true;

  public ContainerJsonWriterIterable(List<Container> containers,
      ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate) {
    super(containers, converterTemplate, containerTemplate);
  }

  @Override
  protected String fetchFirstLine() {
    return "{ \"" + containerTemplate.getJsonKey() + "\" : [";
  }

  @Override
  protected String fetchContainerLine(Container container) {
    StringBuilder stringBuilder = new StringBuilder();
    if (isFirstElement) {
      isFirstElement = false;
    } else {
      stringBuilder.append(",");
    }
    stringBuilder.append("\t { ");
    AtomicBoolean hasElements = new AtomicBoolean(false);
    containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> {
      String attributeValue = container.getAttributeValue(attributeTemplate);
      if (attributeValue != null) {
        hasElements.set(true);
        stringBuilder.append(" \"" + attributeTemplate.getJsonKey() + "\" : ");
        stringBuilder.append("\"" + attributeValue + "\",");
      }
    });
    if (hasElements.get()) {
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    }
    stringBuilder.append(" }");

    return stringBuilder.toString();

  }

  @Override
  public ContainerFileWriterIterable setIfIsFirstLine(boolean isFirstLine) {
    this.isFirstElement = isFirstLine;
    return super.setIfIsFirstLine(isFirstLine);
  }

}
