package de.samply.container.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.samply.template.conversion.AttributeTemplate;
import de.samply.template.conversion.ContainerTemplate;
import de.samply.template.conversion.ConversionTemplate;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ConversionTemplateTest {

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
    ConversionTemplate conversionTemplate = new ConversionTemplate("test-template", "my-test.xml",
        containerTemplates);
    List<String> fhirRevIncludes = generateFhirRevIncludes();
    conversionTemplate.setFhirRevIncludes(fhirRevIncludes);
    var jsonFile = new File(filepath + ".json");
    objectMapper.writeValue(jsonFile, conversionTemplate);
    ConversionTemplate conversionTemplate2 = objectMapper.readValue(jsonFile,
        ConversionTemplate.class);
    var xmlFile = new File(filepath + ".xml");
    xmlMapper.writeValue(xmlFile, conversionTemplate);
    ConversionTemplate conversionTemplate3 = xmlMapper.readValue(xmlFile, ConversionTemplate.class);

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
    return new AttributeTemplate(j, "col-" + extension, "col-" + extension,
        "path.to.attribute." + extension);
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
