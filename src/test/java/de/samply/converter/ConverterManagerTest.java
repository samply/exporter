package de.samply.converter;

import static org.junit.jupiter.api.Assertions.*;

import de.samply.result.container.template.ResultTemplate;
import de.samply.result.container.template.ResultTemplateManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Disabled
class ConverterManagerTest {

  @Test
  void getConverter() {
    ConverterManager converterManager = new ConverterManager();
    Converter converter = converterManager.getConverter(Format.FHIR_QUERY, Format.CSV);
    ResultTemplateManager resultTemplateManager = new ResultTemplateManager("./templates");
    ResultTemplate resultTemplate = resultTemplateManager.getResultTemplate("test-template1");
    Flux flux = converter.convert(Flux.just("Patient"), resultTemplate);
    flux.blockLast();
    //TODO
  }
}
