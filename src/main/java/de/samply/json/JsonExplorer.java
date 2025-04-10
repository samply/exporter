package de.samply.json;

import de.samply.explorer.TaggedLinesExplorer;
import de.samply.exporter.ExporterConst;
import de.samply.template.ConverterTemplateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;


@Component
public class JsonExplorer extends TaggedLinesExplorer {

    private final Set<String> compatibleFileExtensions = Set.of("json");

    public JsonExplorer(
            @Value(ExporterConst.TEMPORAL_FILE_DIRECTORY_SV) String directory,
            ConverterTemplateUtils converterTemplateUtils) {
        super(directory, converterTemplateUtils);
    }

    @Override
    protected String editLine(String line, int lineNumber, boolean isFirstElement, boolean isLastLine) {
        return (lineNumber > 2 && isFirstElement && !isLastLine) ? line.substring(1) : line; // remove initial comma
    }

    @Override
    protected boolean isLastLine(String line) {
        return line.equals("]}");
    }

    @Override
    protected Optional<String> fetchPivotValueFromLine(String pivotAttribute, String line) {
        String token = pivotAttribute + "\" : \"";
        int index = line.indexOf(token);
        if (index > 0) {
            index += token.length();
            int index2 = line.substring(index).indexOf("\"");
            if (index2 > 0) {
                return Optional.of(line.substring(index, index + index2));
            }
        }
        return Optional.empty();
    }

    @Override
    public Set<String> getCompatibleFileExtensions() {
        return compatibleFileExtensions;
    }

}
