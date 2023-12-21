package de.samply.fhir.cql;

import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;

public record CqlQuery(
        Measure measure,
        Library library
) {
}
