package de.samply.converter;

import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;

import java.util.Set;

@Disabled
@SpringBootTest
@Import(ConvertersTestConfiguration.class)
class ConverterManagerTest {

    private final String sourceId = "blaze-store";
    private String targetId;
    private final String CONVERTER_APPLICATION_CONTEXT_PATH = "./converter/converter.xml";


    @Autowired
    private ApplicationContext applicationContext;


    @Test
    void getConverter() {
        ConverterManager converterManager = new ConverterManager(applicationContext, CONVERTER_APPLICATION_CONTEXT_PATH);
        Converter converter = converterManager.getBestMatchConverter(Format.FHIR_QUERY, Format.CSV,
                sourceId, targetId);
        ConverterTemplateManager converterTemplateManager = new ConverterTemplateManager("./templates");

        Set<String> sourceIds = converterManager.getSourceIds();
        //TODO
        Set<String> converterTemplateIds = converterTemplateManager.getConverterTemplateIds();
        //TODO

        ConverterTemplate converterTemplate = converterTemplateManager.getConverterTemplate(
                "test-template1");
        Flux flux = converter.convert(Flux.just("Patient"), converterTemplate);
        flux.blockLast();
        //TODO

    }

}
