package de.samply.converter;

import de.samply.container.Containers;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MeasureReport;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public enum Format {

    FHIR_SEARCH(String.class, true),
    CQL(String.class, true),
    CQL_DATA(String.class, true),
    MEASURE_REPORT(MeasureReport.class, false),
    BUNDLE(Bundle.class, false),
    CONTAINERS(Containers.class, false),
    CSV(Path.class, false),
    EXCEL(Path.class, false),
    OPAL(Path.class, false),
    JSON(Path.class, false),
    XML(Path.class, false);

    private Class zClass;
    private boolean isQuery;

    Format(Class zClass, boolean isQuery) {
        this.zClass = zClass;
        this.isQuery = isQuery;
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

    public boolean isPath() {
        return zClass == Path.class;
    }

    public static String[] fetchQueries() {
        return Arrays.stream(values()).filter(Format::isQuery).map(Enum::name).toArray(String[]::new);
    }

    public static String[] fetchNoQueries() {
        return Arrays.stream(values()).filter(format -> !format.isQuery()).map(Enum::name).toArray(String[]::new);
    }

}
