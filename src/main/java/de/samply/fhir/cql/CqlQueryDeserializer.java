package de.samply.fhir.cql;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;


public class CqlQueryDeserializer extends StdDeserializer<CqlQuery> {

    private IParser fhirParser = FhirContext.forR4().newJsonParser();

    public CqlQueryDeserializer() {
        this(CqlQuery.class);
    }

    public CqlQueryDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CqlQuery deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        JsonNode jsonNode = jsonParser.readValueAsTree();
        Measure measure = fetchNode(jsonParser, jsonNode, "measure", Measure.class);
        Library library = fetchNode(jsonParser, jsonNode, "lib", Library.class);
        return new CqlQuery(measure, library);
    }

    private <T extends IBaseResource> T fetchNode(JsonParser jsonParser, JsonNode jsonNode, String fieldName, Class<T> fieldClass) throws JacksonException {
        JsonNode measureNode = jsonNode.get(fieldName);
        String nodeAsString = measureNode.toString();
        return fhirParser.parseResource(fieldClass, nodeAsString);
    }

}
