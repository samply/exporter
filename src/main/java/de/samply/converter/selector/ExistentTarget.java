package de.samply.converter.selector;

import de.samply.converter.ConverterGroup;
import de.samply.converter.SourceConverter;
import de.samply.converter.TargetConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExistentTarget implements ConverterSelectorCriteria {

  private String targetId;

  public ExistentTarget(String targetId) {
    this.targetId = targetId;
  }

  @Override
  public List<ConverterGroup> getSuitableConverterGroups(List<ConverterGroup> converterGroups) {
    List<ConverterGroup> results = new ArrayList<>();
    converterGroups.forEach(converterGroup -> {
      if (isSuitable(converterGroup)) {
        results.add(converterGroup);
      }
    });
    return results;
  }

  private boolean isSuitable(ConverterGroup converterGroup) {
    AtomicBoolean result = new AtomicBoolean(false);
    converterGroup.getConverters().forEach(converter -> {
      if (converter instanceof TargetConverter && ((TargetConverter) converter).getTargetId()
          .equals(targetId)) {
        result.set(true);
      }
    });
    return result.get();
  }

}
