package de.samply.explorer;

import de.samply.csv.CsvConfig;
import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplateUtils;
import de.samply.template.token.TokenContext;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class ExplorerImpl implements Explorer {

    protected ConverterTemplateUtils converterTemplateUtils;
    protected CsvConfig csvConfig;
    protected Path directory;

    public ExplorerImpl(String directory, ConverterTemplateUtils converterTemplateUtils) {
        this.converterTemplateUtils = converterTemplateUtils;
        this.directory = Path.of(directory);
        csvConfig = new CsvConfig(StandardCharsets.UTF_8, System.lineSeparator(), ExporterConst.DEFAULT_CSV_SEPARATOR);
    }

    protected abstract List<String> fetchLines(Path source, Pivot[] pivots) throws ExplorerException;

    @Override
    public Path filter(Path source, Pivot[] pivots, TokenContext tokenContext) throws ExplorerException {
        Path result = createFilteredVersionOfSourcePath(source, tokenContext);
        writeLinesInPath(fetchLines(source, pivots), result);
        return result;
    }

    private Path createFilteredVersionOfSourcePath(Path source, TokenContext tokenContext) {
        String filename = source.getFileName().toString();
        int index = filename.lastIndexOf(".");
        String extension = filename.substring(index);
        filename = filename.substring(0, index);
        return directory.resolve(
                converterTemplateUtils.replaceTokens(filename + "-filtered-at-${TIMESTAMP}" + extension, tokenContext));
    }

    private void writeLinesInPath(List<String> lines, Path path) throws ExplorerException {
        try {
            Files.write(path, lines);
        } catch (IOException e) {
            throw new ExplorerException(e);
        }
    }

}
