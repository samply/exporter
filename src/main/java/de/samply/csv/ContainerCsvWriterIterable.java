package de.samply.csv;

import de.samply.container.Container;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ContainerCsvWriterIterable implements Iterable<String> {

  private List<Container> containers;
  private ContainerTemplate containerTemplate;
  private ConverterTemplate converterTemplate;
  private boolean headersExists;

  public ContainerCsvWriterIterable(List<Container> containers, ConverterTemplate converterTemplate, ContainerTemplate containerTemplate, boolean headersExists) {
    this.containers = containers;
    this.converterTemplate = converterTemplate;
    this.containerTemplate = containerTemplate;
    this.headersExists = headersExists;
  }

  @NotNull
  @Override
  public Iterator<String> iterator() {
    return new ContainerWriterIterator();
  }

  private class ContainerWriterIterator implements Iterator<String>{

    private boolean isHeaderLine = !headersExists;
    private Iterator<Container> containerIterator;

    public ContainerWriterIterator() {
      this.containerIterator = containers.iterator();
    }

    @Override
    public boolean hasNext() {
      return isHeaderLine ? true : containerIterator.hasNext();
    }

    @Override
    public String next() {
      String line = (isHeaderLine) ? fetchHeaderLine() : fetchAttributeLine();
      isHeaderLine = false;
      return line;
    }

    private String fetchHeaderLine(){
      List<AttributeTemplate> templates = containerTemplate.getAttributeTemplates();
      StringBuilder stringBuilder = new StringBuilder();
      templates.forEach(attributeTemplate -> {
        stringBuilder.append(attributeTemplate.getCsvColumnName());
        stringBuilder.append(converterTemplate.getCsvSeparator());
      });
      stringBuilder.deleteCharAt(stringBuilder.length()-1);
      return stringBuilder.toString();
    }

    private String fetchAttributeLine(){
      Container container = containerIterator.next();
      List<AttributeTemplate> templates = containerTemplate.getAttributeTemplates();
      StringBuilder stringBuilder = new StringBuilder();
      templates.forEach(attributeTemplate -> {
        String attributeValue = container.getAttributeValue(attributeTemplate);
        if (attributeValue == null){
          attributeValue = "";
        }
        stringBuilder.append(attributeValue);
        stringBuilder.append(converterTemplate.getCsvSeparator());
      });
      stringBuilder.deleteCharAt(stringBuilder.length()-1);
      return stringBuilder.toString();
    }

  }

}
