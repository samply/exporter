package de.samply.fhir.cql;

import de.samply.converter.ConverterException;
import de.samply.converter.Format;
import de.samply.template.ConverterTemplate;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MeasureReport;

public class CqlToBundleConverter extends CqlRelatedConverter<Bundle> {

    public CqlToBundleConverter(String fhirStoreUrl, String sourceId) throws ConverterException {
        super(fhirStoreUrl, sourceId);
    }

    public CqlToBundleConverter(String fhirStoreUrl, String sourceId, String httpProxy, String noProxy) throws ConverterException {
        super(fhirStoreUrl, sourceId, httpProxy, noProxy);
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
