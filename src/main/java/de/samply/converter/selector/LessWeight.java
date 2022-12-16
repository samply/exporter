package de.samply.converter.selector;

import de.samply.converter.ConverterGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LessWeight implements ConverterSelectorCriteria {

  @Override
  public List<ConverterGroup> getSuitableConverterGroups(List<ConverterGroup> converterGroups) {
    List<ConverterGroup> results = new ArrayList<>();
    AtomicReference<ConverterGroup> reference = new AtomicReference();
    AtomicInteger lessWeight = new AtomicInteger(-1);
    converterGroups.forEach(converterGroup -> {
      int currentWeight = converterGroup.getConverters().size();
      if (lessWeight.get() == -1 || lessWeight.get() > currentWeight){
        reference.set(converterGroup);
        lessWeight.set(currentWeight);
      }
    });

    results.add(reference.get());
    return results;
  }

}
