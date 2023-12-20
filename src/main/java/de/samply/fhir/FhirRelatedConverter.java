package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.converter.EmptySession;
import de.samply.converter.SourceConverterImpl;


public abstract class FhirRelatedConverter<O> extends SourceConverterImpl<String, O, EmptySession> {

    protected final IGenericClient client;
    protected final String sourceId;

    public FhirRelatedConverter(String fhirStoreUrl, String sourceId) {
        this.client = createFhirClient(fhirStoreUrl);
        this.sourceId = sourceId;
    }

    private IGenericClient createFhirClient(String blazeStoreUrl) {
        // TODO: set proxy
        return FhirContext.forR4().newRestfulGenericClient(blazeStoreUrl);
    }

    @Override
    public String getSourceId() {
        return this.sourceId;
    }


}
