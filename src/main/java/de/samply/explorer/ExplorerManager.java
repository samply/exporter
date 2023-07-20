package de.samply.explorer;

import de.samply.csv.CsvExplorer;
import de.samply.json.JsonExplorer;
import de.samply.xml.XmlExplorer;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class ExplorerManager {

    private final List<Explorer> explorers;

    public ExplorerManager(
            CsvExplorer csvExplorer,
            JsonExplorer jsonExplorer,
            XmlExplorer xmlExplorer
    ) {
        explorers = List.of(csvExplorer, jsonExplorer, xmlExplorer);
    }

    public Optional<Explorer> getExplorer(Path file) {
        return getExplorer(fetchExtension(file));
    }

    private String fetchExtension(Path file) {
        String filename = file.getFileName().toString();
        int index = filename.lastIndexOf(".");
        return filename.substring(index + 1);
    }

    public Optional<Explorer> getExplorer(String fileExtension) {
        for (Explorer explorer : explorers) {
            if (explorer.getCompatibleFileExtensions().contains(fileExtension)) {
                return Optional.of(explorer);
            }
        }
        return Optional.empty();
    }

}
