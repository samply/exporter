package de.samply.template.graph.factory;

import de.samply.converter.Format;
import org.springframework.stereotype.Component;

@Component
public class JsonConverterTemplateGraphFactory extends ConverterTemplateGraphFactory {

    public JsonConverterTemplateGraphFactory() {
        super(new JsonContainerAttributeKeys());
    }

    @Override
    protected Format getOutputFormat() {
        return Format.JSON;
    }

}
