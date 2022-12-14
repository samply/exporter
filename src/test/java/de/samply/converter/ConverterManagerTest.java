package de.samply.converter;

import de.samply.template.conversion.ConversionTemplate;
import de.samply.template.conversion.ConversionTemplateManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Disabled
class ConverterManagerTest {

  @Test
  void getConverter() {
    ConverterManager converterManager = new ConverterManager();
    Converter converter = converterManager.getConverter(Format.FHIR_QUERY, Format.CSV);
    ConversionTemplateManager conversionTemplateManager = new ConversionTemplateManager("./templates");
    ConversionTemplate conversionTemplate = conversionTemplateManager.getConversionTemplate("test-template1");
    Flux flux = converter.convert(Flux.just("Patient"), conversionTemplate);
    flux.blockLast();
    //TODO
  }
}
