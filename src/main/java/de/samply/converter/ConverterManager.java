package de.samply.converter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.csv.ContainersToCsvConverter;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.teiler.TeilerConst;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ConverterManager {

  Table<Format, Format, List<Converter>> allConvertersCombinationsTable = HashBasedTable.create();

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

  private void addConverterToAllConvertersCombinatiosTable (Converter converter){
    List<Converter> converters = allConvertersCombinationsTable.get(converter.getInputFormat(),
        converter.getOutputFormat());
    if (converters == null){
      converters = new ArrayList<>();
      allConvertersCombinationsTable.put(converter.getInputFormat(), converter.getOutputFormat(), converters);
    }
    converters.add(converter);
  }

  private List<Converter> generateConvertersListIfCompatibles(List<Integer> integers,
      List<Converter> converters) {
    List<Converter> results = new ArrayList<>();
    boolean areCompatibles = true;
    for (int i = 0; i < integers.size() - 1; i++) {
      if (!areCompatible(converters.get(integers.get(i)), converters.get(integers.get(i+1)))) {
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


}
