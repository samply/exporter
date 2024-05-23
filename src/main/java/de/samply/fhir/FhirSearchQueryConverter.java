package de.samply.fhir;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.gclient.IQuery;
import de.samply.converter.EmptySession;
import de.samply.converter.Format;
import de.samply.exporter.ExporterConst;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.template.ConverterTemplate;
import de.samply.template.token.TokenContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


public class FhirSearchQueryConverter extends FhirRelatedConverter<Bundle> {

    private final static Logger logger = BufferedLoggerFactory.getLogger(FhirSearchQueryConverter.class);
    private int pageSize = ExporterConst.DEFAULT_FHIR_PAGE_SIZE;

    public FhirSearchQueryConverter(String fhirStoreUrl, String sourceId) {
        super(fhirStoreUrl, sourceId);
    }


    @Override
    public Flux<Bundle> convert(String fhirSearchQuery, ConverterTemplate template, EmptySession session) {
        return Flux.generate(
                () -> {
                    logger.info("Fetching first bundle...");
                    return new BundleContext(2, "");
                },
                (bundleContext, sync) -> {
                    String nextUrl = bundleContext.nextUrl();
                    Bundle bundle = (nextUrl.isEmpty()) ? fetchFirstBundle(fhirSearchQuery, template)
                            : fetchNextBundle(nextUrl);
                    sync.next(bundle);
                    nextUrl = getNextBundleUrl(bundle);
                    if (nextUrl == null) {
                        logger.info("Last bundle fetched");
                        sync.complete();
                    } else {
                        Optional<Integer> numberOfPages = getNumberOfPages(bundle, nextUrl);
                        String part2 = (numberOfPages.isPresent()) ? " of " + numberOfPages.get() +
                                getPercentage(bundleContext.page(), numberOfPages.get()) +
                                calculateRemainingTime(bundleContext.page(), numberOfPages.get(),
                                        bundleContext.instant())
                                : "";
                        logger.info("Fetching bundle " + (bundleContext.page()) + part2);
                    }
                    return new BundleContext(bundleContext.page() + 1, nextUrl);
                });
    }

    private record BundleContext(Integer page, String nextUrl, Instant instant) {

        public BundleContext(Integer page, String nextUrl) {
            this(page, nextUrl, Instant.now());
        }
    }

    private String getPercentage(Integer page, Integer total) {
        String result = "";
        if (page != null && total != null) {
            Double percentage = 100.0 * page / total;
            result = " (" + percentage.intValue() + "%)";
        }
        return result;
    }

    private String calculateRemainingTime(Integer page, Integer total, Instant previousInstant) {
        String result = "";
        if (page != null && total != null && previousInstant != null && page < total) {
            long pageDurationInSeconds = Duration.between(previousInstant, Instant.now()).toSeconds();
            long remainingTimeInSeconds = pageDurationInSeconds * (total - page + 1);
            result = " [remaining time: ";
            if (remainingTimeInSeconds < 1) {
                result = "1 min";
            }
            if (remainingTimeInSeconds < 60) {
                result += remainingTimeInSeconds + " sec";
            } else if (remainingTimeInSeconds < 60 * 60) {
                result += remainingTimeInSeconds / 60 + " min";
            } else {
                long remainingTimeInHours = remainingTimeInSeconds / (60 * 60);
                result += remainingTimeInHours + " hour" + ((remainingTimeInHours != 1) ? "s" : "");
            }
            result += "]";
        }
        return result;
    }

    private Optional<Integer> getNumberOfPages(Bundle bundle, String nextUrl) {
        if (bundle != null && nextUrl != null && bundle.getTotal() > 0) {
            Optional<Integer> pageSize = getPageSize(nextUrl);
            if (pageSize.isPresent()) {
                return Optional.of(
                        Double.valueOf(Math.ceil(1.0 * bundle.getTotal() / pageSize.get())).intValue());
            }
        }
        return Optional.empty();
    }

    private Optional<Integer> getPageSize(String nextUrl) {
        try {
            Integer result = null;
            if (nextUrl != null) {
                int index = nextUrl.indexOf(ExporterConst.BLAZE_URL_QUERY_PARAMETER_PAGE_SIZE);
                if (index > 0) {
                    int index2 = nextUrl.substring(index).indexOf("&");
                    String total =
                            (index2 > 0) ? nextUrl.substring(
                                    index + ExporterConst.BLAZE_URL_QUERY_PARAMETER_PAGE_SIZE.length(),
                                    index + index2) : nextUrl.substring(index);
                    result = Integer.valueOf(total);
                }
            }
            return Optional.ofNullable(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    @Override
    protected EmptySession initializeSession(ConverterTemplate template, TokenContext tokenContext) {
        return EmptySession.instance();
    }

    private Bundle fetchFirstBundle(String fhirSearchQuery, ConverterTemplate template) {
        return fetchBundle(() -> fetchFirstBundleWithoutHandlingException(fhirSearchQuery, template));
    }

    private Bundle fetchFirstBundleWithoutHandlingException(String fhirSearchQuery, ConverterTemplate template) {
        IQuery<IBaseBundle> iQuery = client.search().byUrl(fhirSearchQuery);
        addRevIncludes(iQuery, template);
        iQuery.count(pageSize);
        return iQuery.returnBundle(Bundle.class).execute();
    }

    private Bundle fetchNextBundle(String nextBundleUrl) {
        return fetchBundle(() -> client.search().byUrl(nextBundleUrl).returnBundle(Bundle.class).execute());
    }

    private Bundle fetchBundle(BundleSupplier bundleSupplier) {
        for (int i = 0; i < ExporterConst.DEFAULT_MAX_NUMBER_OF_RETRIES; i++) {
            try {
                return bundleSupplier.get();
            } catch (FhirClientConnectionException e) {
                logger.error("Connection refused with FHIR Server. Retrying (" + i + "/" + ExporterConst.DEFAULT_MAX_NUMBER_OF_RETRIES + ")");
                logger.debug(ExceptionUtils.getStackTrace(e));
                if (i + 1 > ExporterConst.DEFAULT_MAX_NUMBER_OF_RETRIES) {
                    throw e;
                }
                try {
                    Thread.sleep(ExporterConst.DEFAULT_TIMEOUT_IN_SECONDS * 1000);
                } catch (InterruptedException ex) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                    throw new RuntimeException(ex);
                }
            }
        }
        throw new RuntimeException("Reached maximal number of retries");
    }

    private interface BundleSupplier {
        Bundle get() throws FhirClientConnectionException;
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

    @Override
    public Format getInputFormat() {
        return Format.FHIR_SEARCH;
    }

    @Override
    public Format getOutputFormat() {
        return Format.BUNDLE;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
