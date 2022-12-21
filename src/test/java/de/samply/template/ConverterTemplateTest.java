package de.samply.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ConverterTemplateTest {

  private String filepath = "./test-templates/my-test";
  private ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
  private XmlMapper xmlMapper = (XmlMapper) new XmlMapper().enable(
      SerializationFeature.INDENT_OUTPUT);

  @Test
  void test() throws IOException {

    List<ContainerTemplate> containerTemplates = new ArrayList<>();
    int counter = 1;
    containerTemplates.add(generateContainerTemplate(counter++));
    containerTemplates.add(generateContainerTemplate(counter++));
    ConverterTemplate converterTemplate = new ConverterTemplate("test-template", "my-test.xml",
        containerTemplates);
    List<String> fhirRevIncludes = generateFhirRevIncludes();
    converterTemplate.setFhirRevIncludes(fhirRevIncludes);
    var jsonFile = new File(filepath + ".json");
    objectMapper.writeValue(jsonFile, converterTemplate);
    ConverterTemplate converterTemplate2 = objectMapper.readValue(jsonFile,
        ConverterTemplate.class);
    var xmlFile = new File(filepath + ".xml");
    xmlMapper.writeValue(xmlFile, converterTemplate);
    ConverterTemplate converterTemplate3 = xmlMapper.readValue(xmlFile, ConverterTemplate.class);

    //TODO
  }

  private ContainerTemplate generateContainerTemplate(int index) {
    List<AttributeTemplate> attributeTemplateList = new ArrayList<>();
    int counter = 1;
    attributeTemplateList.add(generateAttributeTemplate(index, counter++));
    attributeTemplateList.add(generateAttributeTemplate(index, counter++));
    return new ContainerTemplate(index, "container-" + index + ".csv", "Sheet-" + index,
        attributeTemplateList);
  }

  private AttributeTemplate generateAttributeTemplate(int i, int j) {
    String extension = i + "." + j;
    AttributeTemplate attributeTemplate = new AttributeTemplate(j, "col-" + extension,
        "col-" + extension,
        "path.to.attribute." + extension);
    attributeTemplate.setParentFhirPath("path.to.parent.container-"+i);
    return attributeTemplate;
  }

  private List<String> generateFhirRevIncludes(){
    List<String> fhirRevIncludes = new ArrayList<>();
    fhirRevIncludes.add("Observation:patient");
    fhirRevIncludes.add("Condition:patient");
    fhirRevIncludes.add("Specimen:patient");
    fhirRevIncludes.add("Procedure:patient");
    fhirRevIncludes.add("MedicationStatement:patient");
    fhirRevIncludes.add("ClinicalImpression:patient");

    return fhirRevIncludes;
  }

}
