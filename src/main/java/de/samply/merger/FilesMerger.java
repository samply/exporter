package de.samply.merger;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FilesMerger {
    Path merge (List<Path> paths, HttpServletRequest httpServletRequest) throws IOException;
    String getFileExtension();
}
