package de.samply.opal;

import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.template.token.TokenReplacer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerTemplateMatcher {

  private List<String> filenameList = new ArrayList<>();
  private Map<String, ContainerTemplate> filenameContainerTemplateMap = new HashMap<>();
  public ContainerTemplateMatcher(ConverterTemplate converterTemplate) {
    converterTemplate.getContainerTemplates().forEach(containerTemplate -> {
      String filename = TokenReplacer.removeTokens(containerTemplate.getCsvFilename());
      filenameList.add(filename);
      filenameContainerTemplateMap.put(filename, containerTemplate);
    });
    Collections.sort(filenameList, new CsvFilenameComparator());
  }

  public ContainerTemplate getContainerTemplate (String csvFilename){
    for (String filename : filenameList){
      if (TokenReplacer.isSameElement(csvFilename, filename)){
        return filenameContainerTemplateMap.get(filename);
      }
    }
    return null;
  }

  private class CsvFilenameComparator implements Comparator<String>{
    @Override
    public int compare(String o1, String o2) {
      return o1.length() - o2.length();
    }
  }

}
