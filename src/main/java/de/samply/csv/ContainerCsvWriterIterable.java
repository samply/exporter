package de.samply.csv;

import de.samply.container.Container;
import de.samply.files.ContainerFileWriterIterable;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.List;

public class ContainerCsvWriterIterable extends ContainerFileWriterIterable {

  private final String csvSeparatorReplacement;
  public ContainerCsvWriterIterable(List<Container> containers, ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate, String csvSeparatorReplacement) {
    super(containers, converterTemplate, containerTemplate);
    this.csvSeparatorReplacement = csvSeparatorReplacement;
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
      stringBuilder.append(fetchAttributeValue(container, converterTemplate, attributeTemplate));
      stringBuilder.append(converterTemplate.getCsvSeparator());
    });
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    return stringBuilder.toString();
  }

  private String fetchAttributeValue(Container container, ConverterTemplate converterTemplate, AttributeTemplate attributeTemplate){
    String attributeValue = container.getAttributeValue(attributeTemplate);
    return (attributeValue == null) ? "" : replaceEndOfLines(attributeValue);
  }

  private String replaceEndOfLines(String value){
    if (value != null){
      value = value.replaceAll(converterTemplate.getCsvSeparator(), csvSeparatorReplacement);
      value = value.replaceAll("\r\n", "");
      value = value.replaceAll("\n", "");
      value = value.replaceAll("\r", "");
      value = value.replaceAll("\u2028", "");
      value = value.replaceAll("\u2029", "");
    }
    return value;
  }

}
