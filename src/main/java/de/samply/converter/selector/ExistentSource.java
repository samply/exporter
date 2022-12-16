package de.samply.converter.selector;

import de.samply.converter.ConverterGroup;
import de.samply.converter.SourceConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExistentSource implements ConverterSelectorCriteria {

  private String sourceId;

  public ExistentSource(String sourceId) {
    this.sourceId = sourceId;
  }

  @Override
  public List<ConverterGroup> getSuitableConverterGroups(List<ConverterGroup> converterGroups) {
    List<ConverterGroup> results = new ArrayList<>();
    converterGroups.forEach(converterGroup -> {
      if (isSuitable(converterGroup)){
        results.add(converterGroup);
      }
    });
    return results;
  }

  private boolean isSuitable(ConverterGroup converterGroup){
    AtomicBoolean result = new AtomicBoolean(false);
    converterGroup.getConverters().forEach(converter -> {
      if (converter instanceof SourceConverter && ((SourceConverter) converter).getSourceId().equals(sourceId)){
        result.set(true);
      }
    });
    return result.get();
  }

}
