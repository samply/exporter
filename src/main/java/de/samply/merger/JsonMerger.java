package de.samply.merger;

import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
public class JsonMerger extends FilesMergerImpl {

    public JsonMerger(
            @Autowired ConverterTemplateUtils converterTemplateUtils,
            @Value(ExporterConst.MERGE_FILENAME_SV) String defaultMergeFilename,
            @Value(ExporterConst.TEMPORAL_FILE_DIRECTORY_SV) String temporalFileDirectory) {
        super(converterTemplateUtils, defaultMergeFilename, Path.of(temporalFileDirectory));
    }

    @Override
    protected void write(List<Path> input, FileWriter fileWriter) throws IOException {
        fileWriter.write("[\n");
        writeInputLines(input, fileWriter);
        fileWriter.write("\n]");
    }

    @Override
    protected boolean isCommaSeparated() {
        return true;
    }

    @Override
    public String getFileExtension() {
        return ExporterConst.JSON_FILE_EXTENSION;
    }

}
