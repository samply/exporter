package de.samply.converter.selector;

import de.samply.converter.ConverterGroup;
import java.util.List;

public interface ConverterSelectorCriteria {

  List<ConverterGroup> getSuitableConverterGroups (List<ConverterGroup> converterGroups);

}
