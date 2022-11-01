package de.samply.fhir.cql;

import de.samply.converter.EmptySession;
import de.samply.converter.Format;
import de.samply.fhir.FhirRelatedToBundleConverter;
import de.samply.template.ConverterTemplate;
import org.hl7.fhir.r4.model.Bundle;
import reactor.core.publisher.Flux;

public class CqlToBundleConverter extends FhirRelatedToBundleConverter {

    public CqlToBundleConverter(String fhirStoreUrl, String sourceId) {
        super(fhirStoreUrl, sourceId);
    }

    @Override
    public Format getInputFormat() {
        return Format.CQL_QUERY;
    }

    @Override
    protected Flux<Bundle> convert(String cqlQuery, ConverterTemplate template, EmptySession session) {
        return null;
    }

    @Override
    protected EmptySession initializeSession(ConverterTemplate converterTemplate) {
        return EmptySession.instance();
    }

    private void postLibrary( String library ){

    }

    private void postMeasure (String measure){

    }



}
