package de.samply.fhir.cql;

import de.samply.converter.Format;
import de.samply.template.ConverterTemplate;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MeasureReport;

public class CqlToBundleConverter extends CqlRelatedConverter<Bundle> {

    public CqlToBundleConverter(String fhirStoreUrl, String sourceId) {
        super(fhirStoreUrl, sourceId);
    }

    @Override
    public Format getInputFormat() {
        return Format.CQL;
    }

    @Override
    public Format getOutputFormat() {
        return Format.BUNDLE;
    }

    @Override
    protected Bundle convert(ConverterTemplate template, MeasureReport measureReport) {
        return MeasureReportToBundleConverter.convert(measureReport);
    }

}
