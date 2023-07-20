package de.samply.csv;

import de.samply.explorer.Explorer;
import de.samply.explorer.ExplorerException;
import de.samply.explorer.Pivot;
import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplateUtils;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class CsvExplorer implements Explorer {

    private ConverterTemplateUtils converterTemplateUtils;
    private CsvConfig csvConfig;
    private Path directory;
    private Set compatibleFileExtensions = Set.of("csv");

    public CsvExplorer(
            @Value(ExporterConst.TEMPORAL_FILE_DIRECTORY_SV) String directory,
            ConverterTemplateUtils converterTemplateUtils) {
        this.converterTemplateUtils = converterTemplateUtils;
        this.directory = Path.of(directory);
        csvConfig = new CsvConfig(StandardCharsets.UTF_8, System.lineSeparator(),
                ExporterConst.DEFAULT_CSV_SEPARATOR);
    }

    @Override
    public Path filter(Path source, Pivot pivot) throws ExplorerException {
        List<CSVRecord> csvRecords = fetchMatchCsvRecords(source, pivot);
        List<String> lines = new ArrayList<>();
        if (csvRecords.size() > 0) {
            lines.add(fetchHeader(csvRecords.get(0)));
            csvRecords.forEach(csvRecord -> lines.add(fetchLine(csvRecord)));
        }
        Path result = createFilteredVersionOfSourcePath(source);
        writeLinesInPath(lines, result);
        return result;
    }

    private Path createFilteredVersionOfSourcePath(Path source) {
        String filename = source.getFileName().toString();
        int index = filename.lastIndexOf(".");
        String extension = filename.substring(index);
        filename = filename.substring(0, index);
        return directory.resolve(
                converterTemplateUtils.replaceTokens(filename + "-filtered-at-${TIMESTAMP}" + extension));
    }

    private void writeLinesInPath(List<String> lines, Path path) throws ExplorerException {
        try {
            Files.write(path, lines);
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
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


    private List<CSVRecord> fetchMatchCsvRecords(Path source, Pivot pivot) throws ExplorerException {
        try (CsvRecordIterator csvRecordIterator = new CsvRecordIterator(source, csvConfig)) {
            List<CSVRecord> records = new ArrayList<>();
            while (csvRecordIterator.hasNext()) {
                CSVRecord csvRecord = csvRecordIterator.next();
                String value = csvRecord.get(pivot.attribute());
                if (value != null && pivot.value().equals(value)) {
                    records.add(csvRecord);
                }
            }
            return records;
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

    @Override
    public Optional<Pivot> fetchPivot(Path source, String pivotAttribute, int counter)
            throws ExplorerException {
        try (CsvRecordIterator csvRecordIterator = new CsvRecordIterator(source, csvConfig)) {
            int tempCounter = 0;
            while (tempCounter < counter && csvRecordIterator.hasNext()) {
                CSVRecord csvRecord = csvRecordIterator.next();
                tempCounter++;
                if (tempCounter == counter) {
                    return Optional.ofNullable(convert(csvRecord, pivotAttribute));
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

    @Override
    public Set<String> getCompatibleFileExtensions() {
        return compatibleFileExtensions;
    }

    private Pivot convert(CSVRecord csvRecord, String pivotAttribute) {
        String pivotValue = csvRecord.get(pivotAttribute);
        return (pivotValue != null) ? new Pivot(pivotAttribute, pivotValue) : null;
    }

}
