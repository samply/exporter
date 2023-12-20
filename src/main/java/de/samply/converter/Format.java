package de.samply.converter;

import de.samply.container.Containers;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MeasureReport;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public enum Format {

    FHIR_SEARCH(String.class, true, false),
    CQL(String.class, true, false),
    CQL_DATA(String.class, true, false),
    MEASURE_REPORT(MeasureReport.class, false, false),
    BUNDLE(Bundle.class, false, false),
    CONTAINERS(Containers.class, false, false),
    CSV(Path.class, false, true),
    EXCEL(Path.class, false, true),
    OPAL(Path.class, false, true),
    JSON(Path.class, false, true),
    XML(Path.class, false, true);

    private Class zClass;
    private boolean isQuery;
    private boolean isOutput;

    Format(Class zClass, boolean isQuery, boolean isOutput) {
        this.zClass = zClass;
        this.isQuery = isQuery;
        this.isOutput = isOutput;
    }

    public boolean isInstance(Object object) {
        return zClass.isInstance(object);
    }

    public static boolean isExistentQueryFormat(String queryFormat) {
        AtomicBoolean result = new AtomicBoolean(false);
        Arrays.stream(values()).forEach(format -> {
            if (format.isQuery && format.toString().equals(queryFormat)) {
                result.set(true);
            }
        });
        return result.get();
    }

    public boolean isQuery() {
        return isQuery;
    }

    public boolean isOutput() {
        return isOutput;
    }

    public boolean isPath() {
        return zClass == Path.class;
    }

    public static String[] fetchQueries() {
        return Arrays.stream(values()).filter(Format::isQuery).map(Enum::name).toArray(String[]::new);
    }

    public static String[] fetchNoQueries() {
        return Arrays.stream(values()).filter(format -> !format.isQuery()).map(Enum::name).toArray(String[]::new);
    }

    public static String[] fetchOutputs() {
        return Arrays.stream(values()).filter(Format::isOutput).map(Enum::name).toArray(String[]::new);
    }

}
