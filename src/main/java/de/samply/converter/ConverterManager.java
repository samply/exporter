package de.samply.converter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.converter.selector.*;
import de.samply.exporter.ExporterConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConverterManager {

    private final Table<Format, Format, List<Converter>> allConvertersCombinationsTable = HashBasedTable.create();
    private final Set<String> sourceIds;
    private ConverterSelector converterSelector = new ConverterSelector(
            Arrays.asList(new LessWeight()));

    public ConverterManager(
            @Autowired ApplicationContext applicationContext,
            @Value(ExporterConst.CONVERTER_XML_APPLICATION_CONTEXT_PATH_SV) String converterXmlApplicationContextPath
    ) {
        List<Converter> converters = new ArrayList<>();
        converters.addAll(applicationContext.getBeansOfType(Converter.class).values());
        converters.addAll(fetchConvertersFromApplicationContextInFile(converterXmlApplicationContextPath,
                applicationContext));

        loadAllConverterCombinations(converters);
        sourceIds = fetchSourceIds();
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

    private void loadAllConverterCombinations(List<Converter> converters) {
        ConverterUtils.getCombinationsAndPermutations(converters.size())
                .forEach(integers -> {
                    List<Converter> tempConverters =
                            generateConvertersListIfCompatibles(integers, converters);
                    if (!tempConverters.isEmpty()) {
                        addConverterToAllConvertersCombinatiosTable(new ConverterGroup(tempConverters));
                    }
                });
    }

    private void addConverterToAllConvertersCombinatiosTable(Converter converter) {
        List<Converter> converters = allConvertersCombinationsTable.get(converter.getInputFormat(),
                converter.getOutputFormat());
        if (converters == null) {
            converters = new ArrayList<>();
            allConvertersCombinationsTable.put(converter.getInputFormat(), converter.getOutputFormat(),
                    converters);
        }
        converters.add(converter);
    }

    private List<Converter> generateConvertersListIfCompatibles(List<Integer> integers,
                                                                List<Converter> converters) {
        List<Converter> results = new ArrayList<>();
        boolean areCompatibles = true;
        for (int i = 0; i < integers.size() - 1; i++) {
            if (!areCompatible(converters.get(integers.get(i)), converters.get(integers.get(i + 1)))) {
                areCompatibles = false;
                break;
            }
        }
        if (areCompatibles) {
            integers.forEach(i -> results.add(converters.get(i)));
        }

        return results;
    }

    private boolean areCompatible(Converter firstConverter, Converter secondConverter) {
        return firstConverter.getOutputFormat() == secondConverter.getInputFormat();
    }

    public List<Converter> getConverters(Format inputFormat, Format outputFormat) {
        return allConvertersCombinationsTable.get(inputFormat, outputFormat);
    }

    public Converter getBestMatchConverter(Format inputFormat, Format outputFormat) {
        return converterSelector.getBestMatch(
                convertToGroups(getConverters(inputFormat, outputFormat)));
    }

    public Converter getBestMatchConverter(Format inputFormat, Format outputFormat,
                                           List<ConverterSelectorCriteria> criteria) {
        return converterSelector.getBestMatch(criteria,
                convertToGroups(getConverters(inputFormat, outputFormat)));
    }

    public Converter getBestMatchConverter(Format inputFormat, Format outputFormat, String sourceId,
                                           String targetId) {
        List<ConverterSelectorCriteria> criteria = new ArrayList<>();
        if (sourceId != null) {
            criteria.add(new ExistentSource(sourceId));
        }
        if (targetId != null) {
            criteria.add(new ExistentTarget(targetId));
        }
        return getBestMatchConverter(inputFormat, outputFormat, criteria);
    }

    List<ConverterGroup> convertToGroups(List<Converter> converters) {
        List<ConverterGroup> results = new ArrayList<>();
        converters.forEach(converter -> results.add(
                (converter instanceof ConverterGroup) ? (ConverterGroup) converter
                        : new ConverterGroup(Arrays.asList(converter))));
        return results;
    }

    private Set<String> fetchSourceIds() {
        return (Set<String>) allConvertersCombinationsTable.values().stream()
                .flatMap(Collection::stream)
                .map(converter -> (converter instanceof ConverterGroup)
                        ? ((ConverterGroup) converter).getConverters()
                        : Arrays.asList(converter)).flatMap(Collection::stream)
                .filter(converter -> converter instanceof SourceConverter)
                .map(converter -> ((SourceConverter) converter).getSourceId()).collect(
                        Collectors.toSet());
    }

    public Set<String> getSourceIds() {
        return sourceIds;
    }

}
