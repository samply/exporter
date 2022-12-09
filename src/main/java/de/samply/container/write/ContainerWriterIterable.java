package de.samply.container.write;

import de.samply.container.Attribute;
import de.samply.container.Container;
import de.samply.container.template.AttributeTemplate;
import de.samply.container.template.ContainerTemplate;
import de.samply.container.template.ContainersTemplate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ContainerWriterIterable implements Iterable<String> {

  private AttributeOrderComparator attributeOrderComparator = new AttributeOrderComparator();
  private AttributeTemplateComparator attributeTemplateComparator = new AttributeTemplateComparator();

  private List<Container> containers;
  private ContainerTemplate containerTemplate;
  private ContainersTemplate containersTemplate;
  private boolean headersExists;

  public ContainerWriterIterable(List<Container> containers, ContainersTemplate containersTemplate, ContainerTemplate containerTemplate, boolean headersExists) {
    this.containers = containers;
    this.containersTemplate = containersTemplate;
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
      templates.sort(attributeTemplateComparator);
      StringBuilder stringBuilder = new StringBuilder();
      templates.forEach(attributeTemplate -> {
        stringBuilder.append(attributeTemplate.getCsvColumnName());
        stringBuilder.append(containersTemplate.getCsvSeparator());
      });
      stringBuilder.deleteCharAt(stringBuilder.length()-1);
      return stringBuilder.toString();
    }

    private String fetchAttributeLine(){
      Container container = containerIterator.next();
      container.getAttributes().sort(attributeOrderComparator);
      StringBuilder stringBuilder = new StringBuilder();
      container.getAttributes().forEach(attribute -> {
        stringBuilder.append(attribute.getValue());
        stringBuilder.append(containersTemplate.getCsvSeparator());
      });
      stringBuilder.deleteCharAt(stringBuilder.length()-1);
      return stringBuilder.toString();
    }

  }

  private class AttributeOrderComparator implements Comparator<Attribute>{
    @Override
    public int compare(Attribute o1, Attribute o2) {
      return o1.getAttributeTemplate().getOrder().compareTo(o2.getAttributeTemplate().getOrder());
    }
  }

  private class AttributeTemplateComparator implements Comparator<AttributeTemplate>{
    @Override
    public int compare(AttributeTemplate o1, AttributeTemplate o2) {
      return o1.getOrder().compareTo(o2.getOrder());
    }
  }

}
