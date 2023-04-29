package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import de.samply.converter.EmptySession;
import de.samply.converter.Format;
import de.samply.converter.SourceConverterImpl;
import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplate;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import reactor.core.publisher.Flux;


public class FhirQueryToBundleConverter extends SourceConverterImpl<String, Bundle, EmptySession> {

  private final IGenericClient client;
  private final String sourceId;
  private final String fhirProfilUrl;


  public FhirQueryToBundleConverter(String fhirStoreUrl, String sourceId, String fhirProfilUrl) {
    this.client = createFhirClient(fhirStoreUrl);
    this.sourceId = sourceId;
    this.fhirProfilUrl = fhirProfilUrl;
  }

  @Override
  public Flux<Bundle> convert(String fhirQuery, ConverterTemplate template, EmptySession session) {
    return Flux.generate(
        () -> "",
        (nextUrl, sync) -> {
          Bundle bundle = (nextUrl.isEmpty()) ? fetchFirstBundle(fhirQuery, template)
              : fetchNextBundle(nextUrl);
          sync.next(bundle);
          nextUrl = getNextBundleUrl(bundle);
          if (nextUrl == null) {
            sync.complete();
          }
          return nextUrl;
        });
  }

  @Override
  protected EmptySession initializeSession(ConverterTemplate template) {
    return EmptySession.instance();
  }

  private Bundle fetchFirstBundle(String fhirQuery, ConverterTemplate template) {
    IQuery<IBaseBundle> iQuery = client.search().byUrl(fhirQuery);
    addRevIncludes(iQuery, template);
    return iQuery.returnBundle(Bundle.class).execute();
  }

  private Bundle fetchNextBundle(String nextBundleUrl) {
    return client.search().byUrl(nextBundleUrl).returnBundle(Bundle.class).execute();
  }

  private String getNextBundleUrl(Bundle bundle) {
    for (BundleLinkComponent link : bundle.getLink()) {
      if (link.getRelation().equalsIgnoreCase(ExporterConst.FHIR_STORE_NEXT_BUNDLE)) {
        return link.getUrl();
      }
    }
    return null;
  }

  private void addRevIncludes(IQuery iQuery, ConverterTemplate template) {
    template.getFhirRevIncludes().forEach(revInclude -> iQuery.revInclude(new Include(revInclude)));
  }

  private IGenericClient createFhirClient(String blazeStoreUrl) {
    // TODO: set proxy
    return FhirContext.forR4().newRestfulGenericClient(blazeStoreUrl);
  }

  @Override
  public Format getInputFormat() {
    return Format.FHIR_QUERY;
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
