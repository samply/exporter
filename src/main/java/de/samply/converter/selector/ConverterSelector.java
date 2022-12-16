package de.samply.converter.selector;

import de.samply.converter.ConverterGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ConverterSelector {

  private List<ConverterSelectorCriteria> criteria;

  public ConverterSelector(List<ConverterSelectorCriteria> criteria) {
    this.criteria = criteria;
  }

  public ConverterGroup getBestMatch(List<ConverterSelectorCriteria> criteria,
      List<ConverterGroup> converterGroups) {
    AtomicReference<List<ConverterGroup>> reference = new AtomicReference<>(converterGroups);
    Stream.concat(criteria.stream(), this.criteria.stream()).forEach(
        tempCriteria -> reference.set(tempCriteria.getSuitableConverterGroups(reference.get())));

    return reference.get().get(0);
  }

  public ConverterGroup getBestMatch(List<ConverterGroup> converterGroups) {
    return getBestMatch(new ArrayList<>(), converterGroups);
  }

}
