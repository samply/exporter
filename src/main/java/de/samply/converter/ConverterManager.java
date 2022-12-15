package de.samply.converter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.converter.selector.ConverterSelector;
import de.samply.converter.selector.ConverterSelectorCriteria;
import de.samply.converter.selector.ExistentSource;
import de.samply.converter.selector.LessWeight;
import de.samply.csv.ContainersToCsvConverter;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.teiler.TeilerConst;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ConverterManager {

  private final Table<Format, Format, List<Converter>> allConvertersCombinationsTable = HashBasedTable.create();
  private final Set<String> sourceIds;
  private ConverterSelector converterSelector = new ConverterSelector(
      Arrays.asList(new LessWeight()));

  public ConverterManager(
      @Autowired BundleToContainersConverter bundleToContainersConverter,
      @Autowired ContainersToCsvConverter containersToCsvConverter,
      @Value(TeilerConst.CONVERTER_XML_APPLICATION_CONTEXT_PATH_SV) String converterXmlApplicationContextPath
  ) {
    List<Converter> converters = new ArrayList<>();
    converters.add(bundleToContainersConverter);
    converters.add(containersToCsvConverter);
    converters.addAll(fetchConvertersFromApplicationContext(converterXmlApplicationContextPath));

    loadAllConverterCombinations(converters);
    sourceIds = fetchSourceIds();
  }

  private List<Converter> fetchConvertersFromApplicationContext(
      String converterXmlApplicationContextPath) {
    List<Converter> converters = new ArrayList<>();
    ApplicationContext context = new FileSystemXmlApplicationContext(
        converterXmlApplicationContextPath);
    Arrays.stream(context.getBeanDefinitionNames())
        .forEach(beanName -> converters.add((Converter) context.getBean(beanName)));

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

  public Converter getBestMatchConverter(Format inputFormat, Format outputFormat, String sourceId) {
    return getBestMatchConverter(inputFormat, outputFormat,
        Arrays.asList(new ExistentSource(sourceId)));
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
