package de.samply.opal;

import de.samply.container.Container;
import de.samply.csv.ContainerCsvWriterIterable;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;

import java.util.List;

public class ContainerOpalCsvWriterIterable extends ContainerCsvWriterIterable {

    public ContainerOpalCsvWriterIterable(List<Container> containers, ConverterTemplate converterTemplate, ContainerTemplate containerTemplate, String csvSeparatorReplacement) {
        super(containers, converterTemplate, containerTemplate, csvSeparatorReplacement);
    }

    protected String fetchAttributeValue(Container container, ConverterTemplate converterTemplate, AttributeTemplate attributeTemplate) {
        return super.fetchAttributeValue(container, converterTemplate, attributeTemplate).replace("\"", "\"\"");
    }

}
