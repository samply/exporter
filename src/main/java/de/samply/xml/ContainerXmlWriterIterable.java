package de.samply.xml;

import de.samply.container.Container;
import de.samply.files.ContainerFileWriterIterable;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContainerXmlWriterIterable extends ContainerFileWriterIterable {

  public ContainerXmlWriterIterable(List<Container> containers,
      ConverterTemplate converterTemplate,
      ContainerTemplate containerTemplate) {
    super(containers, converterTemplate, containerTemplate);
  }

  @Override
  protected String fetchFirstLine() {
    return "<" + containerTemplate.getXmlRootElement() + ">";
  }

  @Override
  protected String fetchContainerLine(Container container) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("\t<" + containerTemplate.getXmlElement() + ">");
    AtomicBoolean hasElements = new AtomicBoolean(false);
    containerTemplate.getAttributeTemplates().forEach(attributeTemplate -> {
      String attributeValue = container.getAttributeValue(attributeTemplate);
      if (attributeValue != null) {
        hasElements.set(true);
        stringBuilder.append("\n\t\t<" + attributeTemplate.getXmlElement() + ">");
        stringBuilder.append(attributeValue);
        stringBuilder.append("</" + attributeTemplate.getXmlElement() + ">");
      }
    });
    stringBuilder.append("\n\t</" + containerTemplate.getXmlElement() + ">");
    return stringBuilder.toString();
  }

}
