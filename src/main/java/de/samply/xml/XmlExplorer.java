package de.samply.xml;

import de.samply.explorer.TaggedLinesExplorer;
import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class XmlExplorer extends TaggedLinesExplorer {

    private final Set<String> compatibleFileExtensions = Set.of("xml");

    public XmlExplorer(
            @Value(ExporterConst.TEMPORAL_FILE_DIRECTORY_SV) String directory,
            ConverterTemplateUtils converterTemplateUtils) {
        super(directory, converterTemplateUtils);
    }

    @Override
    public Set<String> getCompatibleFileExtensions() {
        return compatibleFileExtensions;
    }

    @Override
    protected boolean isLastLine(String line) {
        return line.startsWith("</");
    }

    @Override
    protected Optional<String> fetchPivotValueFromLine(String pivotAttribute, String line) {
        String token = pivotAttribute + ">";
        int index = line.indexOf(token);
        if (index > 0) {
            index += token.length();
            int index2 = line.substring(index).indexOf("</");
            if (index2 > 0) {
                return Optional.of(line.substring(index, index + index2));
            }
        }
        return Optional.empty();

    }

}
