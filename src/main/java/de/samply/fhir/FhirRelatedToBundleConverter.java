package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.converter.EmptySession;
import de.samply.converter.Format;
import de.samply.converter.SourceConverterImpl;
import org.hl7.fhir.r4.model.Bundle;

public abstract class FhirRelatedToBundleConverter extends SourceConverterImpl<String, Bundle, EmptySession> {

    protected final IGenericClient client;
    protected final String sourceId;

    public FhirRelatedToBundleConverter(String fhirStoreUrl, String sourceId) {
        this.client = createFhirClient(fhirStoreUrl);
        this.sourceId = sourceId;
    }

    private IGenericClient createFhirClient(String blazeStoreUrl) {
        // TODO: set proxy
        return FhirContext.forR4().newRestfulGenericClient(blazeStoreUrl);
    }

    @Override
    public Format getOutputFormat() {
        return Format.BUNDLE;
    }

    @Override
    public String getSourceId() {
        return this.sourceId;
    }


}
