package de.samply.opal;

import de.samply.container.Container;
import de.samply.converter.Format;
import de.samply.csv.ContainersToCsvConverter;
import de.samply.csv.Session;
import de.samply.exporter.ExporterConst;
import de.samply.files.ContainerFileWriterIterable;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import de.samply.template.ConverterTemplateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ContainersToOpalCsvConverter extends ContainersToCsvConverter {

    public ContainersToOpalCsvConverter(
            @Autowired ConverterTemplateUtils converterTemplateUtils,
            @Value(ExporterConst.WRITE_FILE_DIRECTORY_SV) String writeDirectory,
            @Value(ExporterConst.CSV_SEPARATOR_REPLACEMENT_SV) String csvSeparatorReplacement) {
        super(converterTemplateUtils, writeDirectory, csvSeparatorReplacement);
    }

    @Override
    protected ContainerFileWriterIterable createContainerFileWriterIterable(
            List<Container> containers, ConverterTemplate converterTemplate,
            ContainerTemplate containerTemplate, Session session) {
        return new ContainerOpalCsvWriterIterable(containers, converterTemplate, containerTemplate, csvSeparatorReplacement);
    }

    @Override
    public Format getOutputFormat() {
        return Format.OPAL_CSV;
    }


}
