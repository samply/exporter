package de.samply.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.samply.converter.Format;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class SourcesTest {

  private String filepath = "./test-sources/my-test";
  private ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
  private XmlMapper xmlMapper = (XmlMapper) new XmlMapper().enable(
      SerializationFeature.INDENT_OUTPUT);

  private Random random = new Random();

  @Test
  void name() throws IOException {
    Sources sources = generateSources();
    var xmlFile = new File(filepath + ".xml");
    xmlMapper.writeValue(xmlFile, sources);
    Sources generatedSources = xmlMapper.readValue(xmlFile, Sources.class);
    var jsonFile = new File(filepath + ".json");
    objectMapper.writeValue(jsonFile, sources);
    Sources generatedSources2 = objectMapper.readValue(jsonFile, Sources.class);

    //TODO
  }

  private Sources generateSources() {
    Sources sources = new Sources();
    List<Source> sourceList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      sourceList.add(generateSource(i + 1));
    }
    sources.setSources(sourceList);

    return sources;
  }

  private Source generateSource(int index) {
    Source source = new Source();
    source.setId("source-" + index);
    source.setUrl("http://mysource-" + index + ":8080");
    source.setQueryFormats(generateQueryFormats());
    source.setResultFormats(generateResultFormats());

    return source;
  }

  private List<Format> generateQueryFormats() {
    List<Format> queryFormats = new ArrayList<>();
    int numberOfQueryFormats = random.nextInt(Format.values().length) + 1;
    AtomicInteger counter = new AtomicInteger(0);
    List<Format> allQueryFormats = new ArrayList<>(Arrays.asList(Format.values()));
    Collections.shuffle(allQueryFormats);
    allQueryFormats.forEach(queryFormat -> {
      if (counter.getAndIncrement() < numberOfQueryFormats) {
        queryFormats.add(queryFormat);
      }
    });

    return queryFormats;
  }

  private List<Format> generateResultFormats() {
    List<Format> resultFormats = new ArrayList<>();
    int numberOfResultFormats = random.nextInt(Format.values().length) + 1;
    AtomicInteger counter = new AtomicInteger(0);
    List<Format> allResultFormats = new ArrayList<>(Arrays.asList(Format.values()));
    Collections.shuffle(allResultFormats);
    allResultFormats.forEach(resultFormat -> {
      if (counter.getAndIncrement() < numberOfResultFormats) {
        resultFormats.add(resultFormat);
      }
    });

    return resultFormats;
  }

}
