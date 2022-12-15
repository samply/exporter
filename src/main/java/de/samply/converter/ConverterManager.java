package de.samply.converter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.fhir.FhirQueryToBundleConverter;
import de.samply.csv.ContainersToCsvConverter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConverterManager {

  Table<Format, Format, Converter> allConvertersCombinationsTable = HashBasedTable.create();

  public ConverterManager() {
    loadAllConverterCombinations(loadConverters());
  }

  private Table<Format, Format, Converter> loadConverters() {
    Table<Format, Format, Converter> results = HashBasedTable.create();
    results.put(Format.FHIR_QUERY, Format.BUNDLE,
        new FhirQueryToBundleConverter("http://localhost:8091/fhir"));
    results.put(Format.BUNDLE, Format.CONTAINERS, new BundleToContainersConverter());
    results.put(Format.CONTAINERS, Format.CSV, new ContainersToCsvConverter("./output"));
//TODO
    return results;
  }

  private void loadAllConverterCombinations(
      Table<Format, Format, Converter> inputFormatOutputFormatConverterTable) {
    ConverterUtils.getCombinationsAndPermutations(inputFormatOutputFormatConverterTable.size())
        .forEach(integers -> {
          List<Converter> converters =
              generateConvertersListIfCompatibles(integers, inputFormatOutputFormatConverterTable);
          if (!converters.isEmpty()) {
            allConvertersCombinationsTable.put(
                converters.get(0).getInputFormat(),
                converters.get(converters.size() - 1).getOutputFormat(),
                new ConverterGroup(converters));
          }
        });
  }

  private List<Converter> generateConvertersListIfCompatibles(List<Integer> integers,
      Table<Format, Format, Converter> table) {
    List<Converter> allConverters = table.values().stream().toList();
    List<Converter> results = new ArrayList<>();
    boolean areCompatibles = true;
    for (int i = 0; i < integers.size() - 1; i++) {
      if (!areCompatible(allConverters.get(i), allConverters.get(i + 1))) {
        areCompatibles = false;
        break;
      }
    }
    if (areCompatibles) {
      integers.forEach(i -> results.add(allConverters.get(i)));
    }

    return results;
  }

  private boolean areCompatible(Converter firstConverter, Converter secondConverter) {
    return firstConverter.getOutputFormat() == secondConverter.getInputFormat();
  }

  public Converter getConverter(Format inputFormat, Format outputFormat) {
    return allConvertersCombinationsTable.get(inputFormat, outputFormat);
  }


}
