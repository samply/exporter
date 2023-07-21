package de.samply.csv;

import de.samply.explorer.ExplorerException;
import de.samply.explorer.ExplorerImpl;
import de.samply.explorer.Pivot;
import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplateUtils;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Component
public class CsvExplorer extends ExplorerImpl {

    private Set compatibleFileExtensions = Set.of("csv");

    public CsvExplorer(
            @Value(ExporterConst.TEMPORAL_FILE_DIRECTORY_SV) String directory,
            ConverterTemplateUtils converterTemplateUtils) {
        super(directory, converterTemplateUtils);
    }

    @Override
    protected List<String> fetchLines(Path source, Pivot[] pivots) throws ExplorerException {
        List<CSVRecord> csvRecords = fetchMatchCsvRecords(source, pivots);
        List<String> lines = new ArrayList<>();
        if (csvRecords.size() > 0) {
            lines.add(fetchHeader(csvRecords.get(0)));
            csvRecords.forEach(csvRecord -> lines.add(fetchLine(csvRecord)));
        }
        return lines;
    }

    private String fetchHeader(CSVRecord csvRecord) {
        StringBuilder stringBuilder = new StringBuilder();
        csvRecord.toMap().keySet()
                .forEach(header -> stringBuilder.append(header + csvConfig.delimiter()));
        String result = stringBuilder.toString();
        return (result.length() > 0) ? result.substring(0, result.length() - 1) : result;
    }

    private String fetchLine(CSVRecord csvRecord) {
        StringBuilder stringBuilder = new StringBuilder();
        csvRecord.toMap().keySet()
                .forEach(header -> stringBuilder.append(csvRecord.get(header) + csvConfig.delimiter()));
        String result = stringBuilder.toString();
        return (result.length() > 0) ? result.substring(0, result.length() - 1) : result;
    }


    private List<CSVRecord> fetchMatchCsvRecords(Path source, Pivot[] pivots) throws ExplorerException {
        try (CsvRecordIterator csvRecordIterator = new CsvRecordIterator(source, csvConfig)) {
            List<CSVRecord> records = new ArrayList<>();
            if (pivots.length > 0) {
                while (csvRecordIterator.hasNext()) {
                    CSVRecord csvRecord = csvRecordIterator.next();
                    Pivot tempPivot = Arrays.stream(pivots).filter(pivot -> {
                        String value = csvRecord.get(pivot.attribute());
                        return (value != null && value.trim().length() > 0 && pivot.value().equals(value));
                    }).findFirst().orElse(null);
                    if (tempPivot != null) {
                        records.add(csvRecord);
                    }
                }
            }
            return records;
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

    @Override
    public Optional<Pivot[]> fetchPivot(Path source, String pivotAttribute, int pageCounter, int pageSize)
            throws ExplorerException {
        try (CsvRecordIterator csvRecordIterator = new CsvRecordIterator(source, csvConfig)) {
            int tempCounter = 0;
            List<Pivot> results = new ArrayList<>();
            while (tempCounter < pageCounter + pageSize && csvRecordIterator.hasNext()) {
                CSVRecord csvRecord = csvRecordIterator.next();
                tempCounter++;
                if (tempCounter >= pageCounter && tempCounter < pageCounter + pageSize) {
                    Pivot pivot = convert(csvRecord, pivotAttribute);
                    if (pivot != null) {
                        results.add(pivot);
                    }
                }
            }
            return Optional.of(results.toArray(new Pivot[0]));
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

    @Override
    public Set<String> getCompatibleFileExtensions() {
        return compatibleFileExtensions;
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
            return (numberOfLines >= 1) ? numberOfLines - 1 : 0;
        }
    }

    private Pivot convert(CSVRecord csvRecord, String pivotAttribute) {
        String pivotValue = csvRecord.get(pivotAttribute);
        return (pivotValue != null) ? new Pivot(pivotAttribute, pivotValue) : null;
    }

}
