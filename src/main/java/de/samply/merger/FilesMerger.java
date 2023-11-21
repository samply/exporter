package de.samply.merger;

import de.samply.template.token.TokenContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FilesMerger {
    Path merge(List<Path> paths, TokenContext tokenContext) throws IOException;

    String getFileExtension();
}
