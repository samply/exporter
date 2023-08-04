package de.samply.exporter;

import java.nio.file.Path;
import java.util.List;

public record FilesAndNumberOfPagesOfPivot(List<Path> paths, int numberOfPagesOfPivot) {
}
