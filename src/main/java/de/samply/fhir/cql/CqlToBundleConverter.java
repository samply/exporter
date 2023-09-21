package de.samply.fhir.cql;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.MethodOutcome;
import de.samply.converter.EmptySession;
import de.samply.converter.Format;
import de.samply.fhir.FhirRelatedToBundleConverter;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.template.ConverterTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.r4.model.*;
import reactor.core.publisher.Flux;

public class CqlToBundleConverter extends FhirRelatedToBundleConverter {

    private final static Logger logger = BufferedLoggerFactory.getLogger(CqlToBundleConverter.class);


    public CqlToBundleConverter(String fhirStoreUrl, String sourceId) {
        super(fhirStoreUrl, sourceId);
    }

    @Override
    public Format getInputFormat() {
        return Format.CQL_QUERY;
    }

    @Override
    protected Flux<Bundle> convert(String cqlQuery, ConverterTemplate template, EmptySession session) {
        return Flux.just(fetchBundle(cqlQuery, template));
    }

    private Bundle fetchBundle(String cqlQuery, ConverterTemplate template) {
        try {
            return fetchBundleWithoutHandlingException(cqlQuery, template);
        } catch (CqlExtractorException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    private Bundle fetchBundleWithoutHandlingException(String encodedCqlQuery, ConverterTemplate template) throws CqlExtractorException {
        CqlQuery cqlQuery = CqlExtractor.extract(encodedCqlQuery, template);
        postLibrary(cqlQuery.library());
        MeasureReport measureReport = evaluateMeasure(postMeasure(cqlQuery.measure()), template);
        return MeasureReportToBundleConverter.convert(measureReport);
    }

    @Override
    protected EmptySession initializeSession(ConverterTemplate converterTemplate) {
        return EmptySession.instance();
    }

    private void postLibrary(Library library) {
        MethodOutcome methodOutcome = client.create().resource(library).execute();
        logger.info("Posted library: " + methodOutcome.getId().getValue());
    }

    private Measure postMeasure(Measure measure) {
        MethodOutcome methodOutcome = client.create().resource(measure).execute();
        logger.info("Posted Measure: " + methodOutcome.getId().getValue());
        return (Measure) methodOutcome.getResource();
    }

    private MeasureReport evaluateMeasure(Measure measure, ConverterTemplate template) {
        Parameters parameters = (template.getCqlTemplate() != null && template.getCqlTemplate().getMeasureParameters() != null) ?
                CqlUtils.deserializeParameters(template.getCqlTemplate().getMeasureParameters()) : new Parameters();
        Parameters result = client.operation()
                .onInstance(measure.getId())
                .named("$evaluate-measure")
                .withParameters(parameters)
                .cacheControl(new CacheControlDirective().setNoCache(true))
                .withAdditionalHeader("Content-Type", "application/json")
                .execute();
        MeasureReport measureReport = (MeasureReport) result.getParameter().get(0).getResource();
        logger.info("Evaluated Measure Report: " + measureReport.getId());
        return measureReport;
    }


}
