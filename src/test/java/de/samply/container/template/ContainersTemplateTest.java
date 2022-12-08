package de.samply.container.template;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ContainersTemplateTest {

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
    ContainersTemplate containersTemplate = new ContainersTemplate("test-template", "my-test.xml",
        containerTemplates);
    var jsonFile = new File(filepath + ".json");
    objectMapper.writeValue(jsonFile, containersTemplate);
    ContainersTemplate containersTemplate2 = objectMapper.readValue(jsonFile,
        ContainersTemplate.class);
    var xmlFile = new File(filepath + ".xml");
    xmlMapper.writeValue(xmlFile, containersTemplate);
    ContainersTemplate containersTemplate3 = xmlMapper.readValue(xmlFile, ContainersTemplate.class);

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

}
