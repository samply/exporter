package de.samply.explorer;

import de.samply.template.ConverterTemplateUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public abstract class TaggedLinesExplorer extends ExplorerImpl {

    public TaggedLinesExplorer(String directory, ConverterTemplateUtils converterTemplateUtils) {
        super(directory, converterTemplateUtils);
    }

    protected abstract boolean isLastLine(String line);

    protected abstract Optional<String> fetchPivotValueFromLine(String pivotAttribute, String line);

    protected String editLine(String line, int lineNumber, boolean isFirstElement, boolean isLastLine) {
        return line;
    }

    @Override
    protected List<String> fetchLines(Path source, Pivot[] pivots) throws ExplorerException {
        try {
            return fetchLinesWithoutExceptionHandling(source, pivots);
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

    private List<String> fetchLinesWithoutExceptionHandling(Path source, Pivot[] pivots) throws IOException {
        try (Stream<String> lines = Files.lines(source, csvConfig.charset())) {
            long numberOfLines = Files.lines(source).count();
            List<String> resultLines = new ArrayList<>();
            AtomicInteger lineNumber = new AtomicInteger(1);
            AtomicBoolean isFirstElement = new AtomicBoolean(true);
            lines.filter(line -> isLineToBeIncluded(line, lineNumber.getAndIncrement(), pivots)).forEach(line -> {
                line = editLine(line, lineNumber.get(), isFirstElement.get(), lineNumber.get() == numberOfLines + 1);
                if (lineNumber.get() > 2 && isFirstElement.get()) {
                    isFirstElement.set(false);
                }
                resultLines.add(line);
            });
            return resultLines;
        }
    }

    private boolean isLineToBeIncluded(String line, int lineNumber, Pivot[] pivots) {
        return (lineNumber == 1) || (hasLinePivot(line, pivots)) || isLastLine(line);
    }

    private boolean hasLinePivot(String line, Pivot[] pivots) {
        Pivot tempPivot = Arrays.stream(pivots).filter(pivot -> {
            if (line.contains(pivot.attribute())) {
                Optional<String> value = fetchPivotValueFromLine(pivot.attribute(), line);
                return !value.isEmpty() && value.get().equals(pivot.value());
            }
            return false;
        }).findFirst().orElse(null);
        return Objects.nonNull(tempPivot);
    }

    @Override
    public Optional<Pivot[]> fetchPivot(Path source, String pivotAttribute, int pageCounter, int pageSize) throws ExplorerException {
        try {
            return fetchPivotWithoutExceptionHandling(source, pivotAttribute, pageCounter, pageSize);
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

    private Optional<Pivot[]> fetchPivotWithoutExceptionHandling(Path source, String pivotAttribute, int pageCounter, int pageSize) throws IOException {
        try (Stream<String> lines = Files.lines(source, csvConfig.charset())) {
            AtomicInteger tempCounter = new AtomicInteger(0);
            List<Pivot> results = new ArrayList<>();
            lines.filter(line -> {
                int counter = tempCounter.getAndIncrement();
                return counter > pageCounter * pageSize && counter < (pageCounter + 1) * pageSize + 1;
            }).forEach(pivotLine -> {
                Optional<String> pivotValue = fetchPivotValueFromLine(pivotAttribute, pivotLine);
                if (pivotValue.isPresent()) {
                    results.add(new Pivot(pivotAttribute, pivotValue.get()));
                }
            });
            return Optional.of(results.toArray(new Pivot[0]));
        }
    }

    @Override
    public int fetchTotalNumberOfElements(Path source) throws ExplorerException {
        try {
            return fetchTotalNumberOfElementsWithoutExceptionHandling(source);
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

    private int fetchTotalNumberOfElementsWithoutExceptionHandling(Path source) throws IOException {
        try (Stream<String> fileStream = Files.lines(source)) {
            int numberOfLines = (int) fileStream.count();
            return (numberOfLines >= 2) ? numberOfLines - 2 : 0;
        }
    }

}
