package de.samply.fhir.cql;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MeasureReport;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MeasureReportToBundleConverter {

    public static Bundle convert(MeasureReport measureReport) {
        addProfileIfNotExist(measureReport);
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        AtomicInteger counter = new AtomicInteger(1);
        measureReport.getGroup().forEach(measureReportGroupComponent ->
                measureReportGroupComponent.getStratifier().forEach(measureReportGroupStratifierComponent -> {
                    measureReportGroupStratifierComponent.getStratum().forEach(stratifierGroupComponent -> {
                        bundle.addEntry()
                                .setResource(createMeasureReportForParticularGroupElement(
                                        measureReport,
                                        measureReportGroupComponent,
                                        measureReportGroupStratifierComponent,
                                        stratifierGroupComponent,
                                        counter))
                                .setFullUrl(measureReport.getId());
                    });
                })
        );
        return bundle;
    }

    private static MeasureReport createMeasureReportForParticularGroupElement(
            MeasureReport measureReport,
            MeasureReport.MeasureReportGroupComponent groupElement,
            MeasureReport.MeasureReportGroupStratifierComponent stratifier,
            MeasureReport.StratifierGroupComponent stratum,
            AtomicInteger counter
    ) {
        MeasureReport result = CqlUtils.clone(MeasureReport.class, measureReport);
        result.setId(createId(measureReport, counter.getAndIncrement()));
        filterOtherElements(result.getGroup(), groupElement, g -> g.getCode().getText());
        filterOtherElements(result.getGroup().get(0).getStratifier(), stratifier, s -> s.getCode().get(0).getText());
        filterOtherElements(result.getGroup().get(0).getStratifier().get(0).getStratum(), stratum, s -> s.getValue().getText());
        return result;
    }

    private interface ElementIdGetter<I,O extends Object>{
        O get(I e);
    }

    private static <I,O> void filterOtherElements(List<I> list, I element, ElementIdGetter<I,O> elementIdGetter){
        List<I> tempList = list.stream().filter(i -> elementIdGetter.get(i).equals(elementIdGetter.get(element))).toList();
        list.clear();
        list.addAll(tempList);
    }

    private static IdType createId(MeasureReport measureReport, int counter) {
        IdType idElement = measureReport.getIdElement();
        return new IdType(idElement.getBaseUrl(), idElement.getResourceType(), idElement.getIdPart() + counter, idElement.getVersionIdPart());
    }

    private static void addProfileIfNotExist(MeasureReport measureReport) {
        List<CanonicalType> profile = measureReport.getMeta().getProfile();
        if (profile.isEmpty()) {
            CanonicalType canonicalType = new CanonicalType();
            canonicalType.setValue("http://hl7.org/fhir/StructureDefinition/MeasureReport");
            profile.add(canonicalType);
        }
    }

}
