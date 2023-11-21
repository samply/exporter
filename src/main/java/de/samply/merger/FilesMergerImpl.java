package de.samply.merger;

import de.samply.template.ConverterTemplateUtils;
import de.samply.template.token.TokenContext;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class FilesMergerImpl implements FilesMerger {

    private final ConverterTemplateUtils converterTemplateUtils;
    private final String defaultFilename;
    private final Path temporalDirectory;

    public FilesMergerImpl(ConverterTemplateUtils converterTemplateUtils, String defaultFilename, Path temporalDirectory) {
        this.converterTemplateUtils = converterTemplateUtils;
        this.defaultFilename = defaultFilename;
        this.temporalDirectory = temporalDirectory;
    }

    protected abstract void write(List<Path> input, FileWriter fileWriter) throws IOException;

    protected abstract boolean isCommaSeparated();

    @Override
    public Path merge(List<Path> paths, TokenContext tokenContext) throws IOException {
        Path output = temporalDirectory.resolve(generateFilename(tokenContext));
        write(paths, output);
        return output;
    }

    private void write(List<Path> input, Path output) throws IOException {
        try (FileWriter fileWriter = new FileWriter(output.toFile())) {
            write(input, fileWriter);
        }
    }

    protected void writeInputLines(List<Path> input, FileWriter fileWriter) throws IOException {
        try {
            AtomicInteger counter = new AtomicInteger(0);
            input.stream().forEach(path -> {
                try {
                    AtomicInteger counter2 = new AtomicInteger(0);
                    Files.lines(path).forEach(line -> {
                        try {
                            if (counter.get() == 0 || counter2.getAndIncrement() > 0) {
                                fileWriter.write('\t');
                            }
                            fileWriter.write(line + '\n');
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    if (counter.incrementAndGet() < input.size()) {
                        fileWriter.write("\t");
                        if (isCommaSeparated()) {
                            fileWriter.write(",");
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            throw new IOException(e);
        }
    }

    protected String generateFilename(TokenContext tokenContext) {
        return converterTemplateUtils.replaceTokens(defaultFilename + '.' + getFileExtension(), tokenContext);
    }

}
