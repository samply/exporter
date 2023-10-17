package de.samply.fhir.cql;

import de.samply.converter.Format;
import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplate;
import org.hl7.fhir.r4.model.MeasureReport;

public class CqlToFhirSearchConverter extends CqlRelatedConverter<String> {

    public CqlToFhirSearchConverter(String fhirStoreUrl, String sourceId) {
        super(fhirStoreUrl, sourceId);
    }

    @Override
    public Format getInputFormat() {
        return Format.CQL;
    }

    @Override
    public Format getOutputFormat() {
        return Format.FHIR_SEARCH;
    }

    @Override
    protected String convert(ConverterTemplate template, MeasureReport measureReport) {
        String reference = measureReport.getGroup().get(0).getPopulation().get(0).getSubjectResults().getReference();
        return generateFhirSearchQuery(template, reference);
    }

    private String generateFhirSearchQuery(ConverterTemplate template, String reference) {
        String defaultFhirSearchQuery = template.getCqlTemplate().getDefaultFhirSearchQuery();
        int index = reference.indexOf(ExporterConst.MEASURE_REPORT_GROUP_POPULATION_SUBJECT_RESULTS_REFERENCE_PREFIX);
        reference = reference.substring(ExporterConst.MEASURE_REPORT_GROUP_POPULATION_SUBJECT_RESULTS_REFERENCE_PREFIX.length() + index);
        return defaultFhirSearchQuery + ((defaultFhirSearchQuery.contains("?")) ? "&" : "?") +
                ExporterConst.FHIR_SEARCH_LIST_PARAMETER + "=" + reference;
    }

}
