package de.samply.converter;

import de.samply.exporter.ExporterConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConverterManager {

    private final Set<String> sourceIds;
    private final ConverterGraph converterGraph;

    public ConverterManager(
            @Autowired ApplicationContext applicationContext,
            @Value(ExporterConst.CONVERTER_XML_APPLICATION_CONTEXT_PATH_SV) String converterXmlApplicationContextPath
    ) {
        List<Converter> converters = new ArrayList<>();
        converters.addAll(applicationContext.getBeansOfType(Converter.class).values());
        converters.addAll(fetchConvertersFromApplicationContextInFile(converterXmlApplicationContextPath,
                applicationContext));
        this.converterGraph = new ConverterGraph(converters);
        sourceIds = fetchSourceIds(converters);
    }

    private List<Converter> fetchConvertersFromApplicationContextInFile(
            String converterXmlApplicationContextPath, ApplicationContext applicationContext) {
        List<Converter> converters = new ArrayList<>();
        ApplicationContext context = new FileSystemXmlApplicationContext(
                converterXmlApplicationContextPath);
        Arrays.stream(context.getBeanDefinitionNames())
                .forEach(beanName -> {
                    if (context.getBean(beanName) instanceof Converter) {
                        Converter converter = (Converter) context.getBean(beanName);
                        converters.add(converter);
                        if (converter instanceof ApplicationContextAware) {
                            ((ApplicationContextAware) converter).setApplicationContext(applicationContext);
                        }
                    }
                });
        return converters;
    }

    public Optional<Converter> getBestMatchConverter(Format inputFormat, Format outputFormat, String sourceId, String targetId) {
        return converterGraph.fetchShortestPath(inputFormat, outputFormat, Optional.ofNullable(sourceId), Optional.ofNullable(targetId));
    }

    private Set<String> fetchSourceIds(List<Converter> converters) {
        Set<String> result = new HashSet<>();
        converters.forEach(converter -> {
            if (converter instanceof SourceConverter) {
                result.add(((SourceConverter) converter).getSourceId());
            }
        });
        return result;
    }

    public Set<String> getSourceIds() {
        return sourceIds;
    }

}
