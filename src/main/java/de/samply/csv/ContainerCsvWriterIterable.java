package de.samply.csv;

import de.samply.container.Container;
import de.samply.files.ContainerFileWriterIterable;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.List;

public class ContainerCsvWriterIterable extends ContainerFileWriterIterable {

  public ContainerCsvWriterIterable(List<Container> containers, ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate) {
    super(containers, converterTemplate, containerTemplate);
  }

  @Override
  protected String fetchFirstLine() {
    List<AttributeTemplate> templates = containerTemplate.getAttributeTemplates();
    StringBuilder stringBuilder = new StringBuilder();
    templates.forEach(attributeTemplate -> {
      stringBuilder.append(attributeTemplate.getCsvColumnName());
      stringBuilder.append(converterTemplate.getCsvSeparator());
    });
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    return stringBuilder.toString();
  }

  @Override
  protected String fetchContainerLine(Container container) {
    List<AttributeTemplate> templates = containerTemplate.getAttributeTemplates();
    StringBuilder stringBuilder = new StringBuilder();
    templates.forEach(attributeTemplate -> {
      String attributeValue = container.getAttributeValue(attributeTemplate);
      if (attributeValue == null) {
        attributeValue = "";
      }
      stringBuilder.append(attributeValue);
      stringBuilder.append(converterTemplate.getCsvSeparator());
    });
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    return stringBuilder.toString();
  }


}
