package de.samply.template.graph.factory;

import de.samply.converter.Format;
import org.springframework.stereotype.Component;

@Component
public class XmlConverterTemplateGraphFactory extends ConverterTemplateGraphFactory {
    public XmlConverterTemplateGraphFactory() {
        super(new XmlContainerAttributeKeys());
    }

    @Override
    protected Format getOutputFormat() {
        return Format.XML;
    }
}
