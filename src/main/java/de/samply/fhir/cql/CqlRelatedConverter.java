package de.samply.fhir.cql;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.MethodOutcome;
import de.samply.converter.EmptySession;
import de.samply.fhir.FhirRelatedConverter;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.template.ConverterTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Parameters;
import reactor.core.publisher.Flux;

public abstract class CqlRelatedConverter<O> extends FhirRelatedConverter<O> {

    private final static Logger logger = BufferedLoggerFactory.getLogger(CqlRelatedConverter.class);

    protected abstract O convert(ConverterTemplate template, MeasureReport measureReport);

    public CqlRelatedConverter(String fhirStoreUrl, String sourceId) {
        super(fhirStoreUrl, sourceId);
    }

    @Override
    protected Flux<O> convert(String cqlQuery, ConverterTemplate template, EmptySession session) {
        return Flux.just(convert(template, fetchMeasureReport(cqlQuery, template)));
    }

    private MeasureReport fetchMeasureReport(String cqlQuery, ConverterTemplate template) {
        try {
            return fetchMeasureReportWithoutHandlingException(cqlQuery, template);
        } catch (CqlExtractorException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    private MeasureReport fetchMeasureReportWithoutHandlingException(String encodedCqlQuery, ConverterTemplate template) throws CqlExtractorException {
        CqlQuery cqlQuery = CqlExtractor.extract(encodedCqlQuery, template);
        postLibrary(cqlQuery.library());
        return evaluateMeasure(postMeasure(cqlQuery.measure()), template);
    }

    @Override
    protected EmptySession initializeSession(ConverterTemplate converterTemplate, HttpServletRequest httpServletRequest) {
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
