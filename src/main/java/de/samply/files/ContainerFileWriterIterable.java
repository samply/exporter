package de.samply.files;

import de.samply.container.Container;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class ContainerFileWriterIterable implements Iterable<String> {

  protected List<Container> containers;
  protected ContainerTemplate containerTemplate;
  protected ConverterTemplate converterTemplate;

  public ContainerFileWriterIterable(List<Container> containers,
      ConverterTemplate converterTemplate, ContainerTemplate containerTemplate) {
    this.containers = containers;
    this.converterTemplate = converterTemplate;
    this.containerTemplate = containerTemplate;
  }

  @NotNull
  @Override
  public Iterator<String> iterator() {
    return new ContainerWriterIterator();
  }

  protected class ContainerWriterIterator implements Iterator<String> {

    protected Iterator<Container> containerIterator;

    public ContainerWriterIterator() {
      this.containerIterator = containers.iterator();
    }

    @Override
    public boolean hasNext() {
      return containerIterator.hasNext();
    }

    @Override
    public String next() {
      return fetchLine(containerIterator.next());
    }

  }

  protected abstract String fetchLine(Container container);

}
