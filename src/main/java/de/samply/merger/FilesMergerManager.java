package de.samply.merger;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class FilesMergerManager {

    private Map<String, FilesMerger> extensionFileMergerMap = new HashMap<>();

    public FilesMergerManager(ApplicationContext applicationContext) {
        applicationContext.getBeansOfType(FilesMerger.class).values().forEach(
                filesMerger -> extensionFileMergerMap.put(filesMerger.getFileExtension(), filesMerger));
    }

    public Optional<Path> merge(List<Path> paths) throws IOException {
        if (paths.size() > 0) {
            FilesMerger filesMerger = extensionFileMergerMap.get(fetchFileExtension(paths.get(0)));
            return Optional.ofNullable((filesMerger != null) ? filesMerger.merge(paths) : null);
        }
        return Optional.empty();
    }

    private String fetchFileExtension(Path path) {
        String filename = path.getFileName().toString();
        int index = filename.lastIndexOf(".");
        return (index >= 0) ? filename.substring(index + 1) : filename;
    }

    public boolean isFileExtensionSupported(List<Path> paths) {
        return (paths.size() > 0) ? extensionFileMergerMap.containsKey(fetchFileExtension(paths.get(0))) : false;
    }


}
