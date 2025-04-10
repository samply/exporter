package de.samply.files;

import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplateUtils;
import de.samply.template.token.TokenContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Session {

    private final ConverterTemplateUtils converterTemplateUtils;
    private final String writeDirectory;
    private final List<Path> pathList = new ArrayList<>();
    private final Map<ContainerTemplate, Path> containerTemplatePathMap = new HashMap<>();
    private final TokenContext tokenContext;

    public Session(ConverterTemplateUtils converterTemplateUtils, String writeDirectory, TokenContext tokenContext) {
        this.converterTemplateUtils = converterTemplateUtils;
        this.writeDirectory = writeDirectory;
        this.tokenContext = tokenContext;
    }


    public Path getFilePath(ContainerTemplate containerTemplate) {
        Path path = containerTemplatePathMap.get(containerTemplate);
        if (path == null) {
            String filename = converterTemplateUtils.replaceTokens(getFilename(containerTemplate), tokenContext);
            path = Paths.get(writeDirectory).resolve(filename);
            containerTemplatePathMap.put(containerTemplate, path);
        }
        return path;
    }

    public List<Path> getNewPaths(List<Path> pathList) {
        List<Path> results = new ArrayList<>();
        if (pathList != null && pathList.size() > 0) {
            pathList.forEach(path -> {
                if (isNew(path)) {
                    results.add(path);
                    this.pathList.add(path);
                }
            });
        }
        return results;
    }

    private boolean isNew(Path path) {
        for (Path tempPath : pathList) {
            if (tempPath.compareTo(path) == 0) {
                return false;
            }
        }
        return true;
    }

    protected abstract String getFilename(ContainerTemplate containerTemplate);

}
