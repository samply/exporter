package de.samply.explorer;

import de.samply.explorer.ExplorerException;
import de.samply.explorer.ExplorerImpl;
import de.samply.explorer.Pivot;
import de.samply.template.ConverterTemplateUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public abstract class TaggedLinesExplorer extends ExplorerImpl {

    public TaggedLinesExplorer(String directory, ConverterTemplateUtils converterTemplateUtils) {
        super(directory, converterTemplateUtils);
    }

    protected abstract boolean isLastLine(String line);

    protected abstract Optional<String> fetchPivotValueFromLine(String pivotAttribute, String line);

    protected String editLine(String line, int lineNumber, boolean isFirstElement) {
        return line;
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
                line = editLine(line, lineNumber.get(), isFirstElement.get());
                if (lineNumber.get() > 2 && isFirstElement.get()) {
                    isFirstElement.set(false);
                }
                resultLines.add(line);
            });
            return resultLines;
        }
    }

    private boolean isLineToBeIncluded(String line, int lineNumber, Pivot pivot) {
        return (lineNumber == 1) || (hasLinePivot(line, pivot)) || isLastLine(line);
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

}
