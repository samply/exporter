package de.samply.json;

import de.samply.container.Container;
import de.samply.files.ContainerFileWriterIterable;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class ContainerJsonWriterIterable extends ContainerFileWriterIterable {

  private boolean isFirstLine = true;
  private boolean isFirstElement = true;

  public ContainerJsonWriterIterable(List<Container> containers,
      ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate) {
    super(containers, converterTemplate, containerTemplate);
  }

  @NotNull
  @Override
  public Iterator<String> iterator() {
    return new ContainerJsonWriterIterator();
  }

  private class ContainerJsonWriterIterator extends ContainerWriterIterator {

    @Override
    public boolean hasNext() {
      return isFirstLine ? true : containerIterator.hasNext();
    }

    @Override
    public String next() {
      String line = (isFirstLine) ? fetchFirstLine() : fetchContainerLine(containerIterator.next());
      isFirstLine = false;
      return line;
    }

  }

  @Override
  protected String fetchLine(Container container) {
    return null;
  }

  private String fetchFirstLine() {
    return "{ \"" + containerTemplate.getJsonKey() + "\" : [";
  }

  private String fetchContainerLine(Container container) {
    StringBuilder stringBuilder = new StringBuilder();
    if (isFirstElement){
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

  public ContainerJsonWriterIterable setIfIsFirstLine(boolean isFirstLine) {
    this.isFirstLine = isFirstLine;
    this.isFirstElement = isFirstLine;
    return this;
  }

}
