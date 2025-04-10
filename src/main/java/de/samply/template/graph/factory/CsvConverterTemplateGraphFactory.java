package de.samply.template.graph.factory;

import de.samply.converter.Format;
import org.springframework.stereotype.Component;

@Component
public class CsvConverterTemplateGraphFactory extends ConverterTemplateGraphFactory {
    public CsvConverterTemplateGraphFactory() {
        super(new CsvContainerAttributeKeys());
    }

    @Override
    protected Format getOutputFormat() {
        return Format.CSV;
    }
}
