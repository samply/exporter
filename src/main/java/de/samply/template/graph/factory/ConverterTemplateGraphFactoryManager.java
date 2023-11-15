package de.samply.template.graph.factory;

import de.samply.converter.Format;
import de.samply.template.ConverterTemplate;
import de.samply.template.graph.ConverterGraph;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ConverterTemplateGraphFactoryManager {

    private Map<Format, ConverterTemplateGraphFactory> formatConverterTemplateGraphFactoryMap = new HashMap<>();

    public ConverterTemplateGraphFactoryManager(ApplicationContext applicationContext) {
        applicationContext.getBeansOfType(ConverterTemplateGraphFactory.class).values().forEach(converterTemplateGraphFactory -> {
            formatConverterTemplateGraphFactoryMap.put(converterTemplateGraphFactory.getOutputFormat(), converterTemplateGraphFactory);
        });
    }

    public Optional<ConverterGraph> fetchConverterTemplateGraph(ConverterTemplate converterTemplate, Format outputFormat){
        ConverterTemplateGraphFactory converterTemplateGraphFactory = formatConverterTemplateGraphFactoryMap.get(outputFormat);
        if (converterTemplateGraphFactory != null){
            return Optional.of(converterTemplateGraphFactory.create(converterTemplate));
        }
        return Optional.empty();
    }

}
