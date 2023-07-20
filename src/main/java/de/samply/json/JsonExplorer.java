package de.samply.json;

import de.samply.explorer.ExplorerException;
import de.samply.explorer.ExplorerImpl;
import de.samply.explorer.Pivot;
import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


@Component
public class JsonExplorer extends ExplorerImpl {

    private final Set<String> compatibleFileExtensions = Set.of("json");

    public JsonExplorer(
            @Value(ExporterConst.TEMPORAL_FILE_DIRECTORY_SV) String directory,
            ConverterTemplateUtils converterTemplateUtils) {
        super(directory, converterTemplateUtils);
    }

    @Override
    protected List<String> fetchLines(Path source, Pivot pivot) throws ExplorerException {
        try {
            return fetchLinesWithoutExceptionHandling(source, pivot);
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

    private List<String> fetchLinesWithoutExceptionHandling(Path source, Pivot pivot) throws IOException {
        try (Stream<String> lines = Files.lines(source, csvConfig.charset())) {
            List<String> resultLines = new ArrayList<>();
            AtomicInteger lineNumber = new AtomicInteger(1);
            AtomicBoolean isFirstElement = new AtomicBoolean(true);
            lines.filter(line -> isLineToBeIncluded(line, lineNumber.getAndIncrement(), pivot)).forEach(line -> {
                if (lineNumber.get() > 2 && isFirstElement.get()) {
                    line = line.substring(1); // remove initial comma
                    isFirstElement.set(false);
                }
                resultLines.add(line);
            });
            return resultLines;
        }
    }

    private boolean isLineToBeIncluded(String line, int lineNumber, Pivot pivot) {
        return (lineNumber == 1) || (hasLinePivot(line, pivot)) || (line.equals("]}"));
    }

    private boolean hasLinePivot(String line, Pivot pivot) {
        if (!line.contains(pivot.attribute())) {
            return false;
        }
        Optional<String> value = fetchPivotValueFromLine(pivot.attribute(), line);
        return !value.isEmpty() && value.get().equals(pivot.value());
    }

    @Override
    public Optional<Pivot> fetchPivot(Path source, String pivotAttribute, int counter) throws ExplorerException {
        try {
            return fetchPivotWithoutExceptionHandling(source, pivotAttribute, counter);
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

    private Optional<Pivot> fetchPivotWithoutExceptionHandling(Path source, String pivotAttribute, int counter) throws IOException {
        try (Stream<String> lines = Files.lines(source, csvConfig.charset())) {
            AtomicInteger tempCounter = new AtomicInteger(0);
            String pivotLine = lines.filter(line -> tempCounter.getAndIncrement() == counter).findFirst().orElse(null);
            if (pivotLine != null) {
                Optional<String> pivotValue = fetchPivotValueFromLine(pivotAttribute, pivotLine);
                if (pivotValue.isPresent()) {
                    return Optional.of(new Pivot(pivotAttribute, pivotValue.get()));
                }
            }
            return Optional.empty();
        }
    }

    private Optional<String> fetchPivotValueFromLine(String pivotAttribute, String line) {
        String token = pivotAttribute + "\" : \"";
        int index = line.indexOf(token);
        if (index > 0) {
            index += token.length();
            int index2 = line.substring(index).indexOf("\"");
            if (index2 > 0) {
                return Optional.of(line.substring(index, index + index2));
            }
        }
        return Optional.empty();
    }

    @Override
    public Set<String> getCompatibleFileExtensions() {
        return compatibleFileExtensions;
    }

}
