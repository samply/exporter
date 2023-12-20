package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import de.samply.converter.EmptySession;
import de.samply.converter.SourceConverterImpl;
import de.samply.exporter.ExporterConst;

public abstract class FhirRelatedConverter<O> extends SourceConverterImpl<String, O, EmptySession> {

    protected IGenericClient client;
    protected final String sourceId;
    private final String fhirStoreUrl;

    public FhirRelatedConverter(String fhirStoreUrl, String sourceId) {
        this.fhirStoreUrl = fhirStoreUrl;
        this.sourceId = sourceId;
        recreateFhirClient();
    }

    protected void recreateFhirClient() {
        this.client = configureHttpClient(FhirContext.forR4()).newRestfulGenericClient(fhirStoreUrl);
    }

    private FhirContext configureHttpClient(FhirContext fhirContext) {
        IRestfulClientFactory restfulClientFactory = fhirContext.getRestfulClientFactory();
        restfulClientFactory.setConnectTimeout(ExporterConst.DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS * 1000);
        restfulClientFactory.setConnectionRequestTimeout(ExporterConst.DEFAULT_CONNECTION_REQUEST_TIMEOUT_IN_SECONDS * 1000);
        restfulClientFactory.setSocketTimeout(ExporterConst.DEFAULT_SOCKET_TIMEOUT_IN_SECONDS * 1000);
        //TODO: Set Proxy
        return fhirContext;
    }

    @Override
    public String getSourceId() {
        return this.sourceId;
    }


}
