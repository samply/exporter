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
public class XmlMerger extends FilesMergerImpl {

    private final String xmlRootElement;

    public XmlMerger(
            @Autowired ConverterTemplateUtils converterTemplateUtils,
            @Value(ExporterConst.XML_FILE_MERGER_ROOT_ELEMENT_SV) String xmlRootElement,
            @Value(ExporterConst.MERGE_FILENAME_SV) String defaultMergeFilename,
            @Value(ExporterConst.TEMPORAL_FILE_DIRECTORY_SV) String temporalFileDirectory) {
        super(converterTemplateUtils, defaultMergeFilename, Path.of(temporalFileDirectory));
        this.xmlRootElement = xmlRootElement;
    }

    @Override
    public String getFileExtension() {
        return ExporterConst.XML_FILE_EXTENSION;
    }

    @Override
    protected void write(List<Path> input, FileWriter fileWriter) throws IOException {
        fileWriter.write("<" + xmlRootElement + ">");
        writeInputLines(input, fileWriter);
        fileWriter.write("</" + xmlRootElement + ">");
    }

    @Override
    protected boolean isCommaSeparated() {
        return false;
    }

}
