package de.samply.merger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FilesMerger {
    Path merge (List<Path> paths) throws IOException;
    String getFileExtension();
}
