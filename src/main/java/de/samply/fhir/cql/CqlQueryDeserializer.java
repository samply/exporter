package de.samply.fhir.cql;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;

import java.io.IOException;

public class CqlQueryDeserializer extends StdDeserializer<CqlQuery> {

    private IParser fhirParser = FhirContext.forR4().newJsonParser();

    public CqlQueryDeserializer() {
        this(null);
    }

    public CqlQueryDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CqlQuery deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode jsonNode = jsonParser.readValueAsTree();
        String language = jsonNode.get("lang").asText();
        Measure measure = fetchNode(jsonParser, jsonNode, "measure", Measure.class);
        Library library = fetchNode(jsonParser, jsonNode, "lib", Library.class);
        return new CqlQuery(language, measure, library);
    }

    private <T extends IBaseResource> T fetchNode(JsonParser jsonParser, JsonNode jsonNode, String fieldName, Class<T> fieldClass) throws JacksonException {
        ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode measureNode = jsonNode.get(fieldName);
        String nodeAsString = objectMapper.writeValueAsString(measureNode);
        return fhirParser.parseResource(fieldClass, nodeAsString);
    }

}
