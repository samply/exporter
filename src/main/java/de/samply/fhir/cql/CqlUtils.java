package de.samply.fhir.cql;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Parameters;

public class CqlUtils {

    private static IParser fhirParser = FhirContext.forR4().newJsonParser();

    public static Parameters deserializeParameters(String parameters) {
        return fhirParser.parseResource(Parameters.class, parameters);
    }

    private static <T extends IBaseResource> T deserialize(Class<T> resourceClass, String element) {
        return fhirParser.parseResource(resourceClass, element);
    }

    public static <T extends IBaseResource> T clone (Class<T> resourceClass, T element){
        String encodedResource = fhirParser.encodeResourceToString(element);
        return fhirParser.parseResource(resourceClass, encodedResource);
    }

}
