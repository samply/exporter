package de.samply.converter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.samply.fhir.BundleToContainersConverter;
import de.samply.fhir.FhirStoreClient;
import de.samply.result.container.write.ContainersCsvWriter;
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
    Table<Format,Format,Converter> results = HashBasedTable.create();
    results.put(Format.FHIR_QUERY, Format.BUNDLE, new FhirStoreClient("http://localhost:8091/fhir"));
    results.put(Format.BUNDLE, Format.CONTAINERS, new BundleToContainersConverter());
    results.put(Format.CONTAINERS, Format.CSV, new ContainersCsvWriter("./output"));
//TODO
    return results;
  }

  private void loadAllConverterCombinations(
      Table<Format, Format, Converter> inputFormatOutputFormatConverterTable) {
    ConverterUtils.getCombinationsAndPermutations(inputFormatOutputFormatConverterTable.size())
        .forEach(integers -> {
          List<TempConverter> tempConverters =
              generateTempConvertersIfCompatibles(integers, inputFormatOutputFormatConverterTable);
          if (!tempConverters.isEmpty()) {
            allConvertersCombinationsTable.put(
                tempConverters.get(0).inputFormat,
                tempConverters.get(tempConverters.size() -1).ouputFormat,
                new ConverterGroup(extractConverters(tempConverters)));
          }
        });
  }

  private List<Converter> extractConverters(List<TempConverter> tempConverters){
    List<Converter> results = new ArrayList<>();
    tempConverters.forEach(tempConverter -> results.add(tempConverter.converter));
    return results;
  }

  private List<TempConverter> generateTempConvertersIfCompatibles(List<Integer> integers,
      Table<Format, Format, Converter> table) {
    List<TempConverter> results = new ArrayList<>();
    List<TempConverter> converters = extractConverters(table);
    boolean areCompatibles = true;
    for (int i = 0; i < integers.size() - 1; i++) {
      if (!areCompatible(converters.get(i), converters.get(i + 1))) {
        areCompatibles = false;
        break;
      }
    }
    if (areCompatibles) {
      integers.forEach(i -> results.add(converters.get(i)));
    }

    return results;
  }

  private List<TempConverter> extractConverters(Table<Format, Format, Converter> table) {
    List<TempConverter> converters = new ArrayList<>();
    table.rowMap().forEach((format, formatConverterMap) ->
        formatConverterMap.forEach((format2, converter) -> {
          converters.add(new TempConverter(format, format2, converter));
        }));
    return converters;
  }

  private record TempConverter(Format inputFormat, Format ouputFormat, Converter converter) {}

  private boolean areCompatible(TempConverter firstConverter, TempConverter secondConverter) {
    return firstConverter.ouputFormat == secondConverter.inputFormat;
  }

  public Converter getConverter(Format inputFormat, Format outputFormat) {
    return allConvertersCombinationsTable.get(inputFormat, outputFormat);
  }


}
