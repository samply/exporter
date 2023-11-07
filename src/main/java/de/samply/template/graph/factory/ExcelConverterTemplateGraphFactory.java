package de.samply.template.graph.factory;

import de.samply.converter.Format;
import org.springframework.stereotype.Component;

@Component
public class ExcelConverterTemplateGraphFactory extends ConverterTemplateGraphFactory{

    public ExcelConverterTemplateGraphFactory() {
        super(new ExcelContainerAttributeKeys());
    }

    @Override
    protected Format getOutputFormat() {
        return Format.EXCEL;
    }

}
