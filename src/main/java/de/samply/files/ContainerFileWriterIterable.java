package de.samply.files;

import de.samply.container.Container;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;

import java.util.Iterator;
import java.util.List;

public abstract class ContainerFileWriterIterable implements Iterable<String> {

    protected List<Container> containers;
    protected ContainerTemplate containerTemplate;
    protected ConverterTemplate converterTemplate;
    private boolean isFirstLine = true;

    public ContainerFileWriterIterable(List<Container> containers,
                                       ConverterTemplate converterTemplate, ContainerTemplate containerTemplate) {
        this.containers = containers;
        this.converterTemplate = converterTemplate;
        this.containerTemplate = containerTemplate;
    }


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
            return isFirstLine ? true : containerIterator.hasNext();
        }

        @Override
        public String next() {
            String line = (isFirstLine) ? fetchFirstLine() : fetchContainerLine(containerIterator.next());
            isFirstLine = false;
            return line;
        }

    }

    protected abstract String fetchFirstLine();

    protected abstract String fetchContainerLine(Container container);

    public ContainerFileWriterIterable setIfIsFirstLine(boolean isFirstLine) {
        this.isFirstLine = isFirstLine;
        return this;
    }

}
