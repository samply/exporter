package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import de.samply.converter.ConverterImpl;
import de.samply.result.container.template.ResultTemplate;
import de.samply.teiler.TeilerConst;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import reactor.core.publisher.Flux;


public class FhirStoreClient extends ConverterImpl<String,Bundle> {

  private final IGenericClient client;


  public FhirStoreClient(String fhirStoreUrl) {
    this.client = createFhirClient(fhirStoreUrl);
  }

  @Override
  public Flux<Bundle> convert(String fhirQuery, ResultTemplate template) {
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

  private Bundle fetchFirstBundle(String fhirQuery, ResultTemplate template) {
    IQuery<IBaseBundle> iQuery = client.search().byUrl(fhirQuery);
    addRevIncludes(iQuery, template);
    return iQuery.returnBundle(Bundle.class).execute();
  }

  private Bundle fetchNextBundle(String nextBundleUrl) {
    return client.search().byUrl(nextBundleUrl).returnBundle(Bundle.class).execute();
  }

  private String getNextBundleUrl(Bundle bundle) {
    for (BundleLinkComponent link : bundle.getLink()) {
      if (link.getRelation().equalsIgnoreCase(TeilerConst.FHIR_STORE_NEXT_BUNDLE)) {
        return link.getUrl();
      }
    }
    return null;
  }

  private void addRevIncludes(IQuery iQuery, ResultTemplate template) {
    template.getFhirRevIncludes().forEach(revInclude -> iQuery.revInclude(new Include(revInclude)));
  }

  private IGenericClient createFhirClient(String blazeStoreUrl) {
    // TODO: set proxy
    return FhirContext.forR4().newRestfulGenericClient(blazeStoreUrl);
  }

}
