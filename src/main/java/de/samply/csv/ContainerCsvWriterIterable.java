package de.samply.csv;

import de.samply.container.Container;
import de.samply.files.ContainerFileWriterIterable;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ContainerCsvWriterIterable extends ContainerFileWriterIterable {

  private boolean headersExists = false;

  public ContainerCsvWriterIterable(List<Container> containers, ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate) {
    super(containers, converterTemplate, containerTemplate);
  }

  @NotNull
  @Override
  public Iterator<String> iterator() {
    return new ContainerCsvWriterIterator();
  }

  private class ContainerCsvWriterIterator extends ContainerWriterIterator{
    private boolean isHeaderLine = !headersExists;

    @Override
    public boolean hasNext() {
      return isHeaderLine ? true : containerIterator.hasNext();
    }

    @Override
    public String next() {
      String line = (isHeaderLine) ? fetchHeaderLine() : fetchAttributeLine(containerIterator.next());
      isHeaderLine = false;
      return line;
    }

    private String fetchHeaderLine() {
      List<AttributeTemplate> templates = containerTemplate.getAttributeTemplates();
      StringBuilder stringBuilder = new StringBuilder();
      templates.forEach(attributeTemplate -> {
        stringBuilder.append(attributeTemplate.getCsvColumnName());
        stringBuilder.append(converterTemplate.getCsvSeparator());
      });
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      return stringBuilder.toString();
    }

    private String fetchAttributeLine(Container container) {
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

  @Override
  protected String fetchLine(Container container) {
    return null;
  }

  public ContainerCsvWriterIterable headerExists(boolean headersExists){
    this.headersExists = headersExists;
    return this;
  }


}
