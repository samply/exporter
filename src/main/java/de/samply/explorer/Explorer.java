package de.samply.explorer;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public interface Explorer {

    Path filter(Path source, Pivot pivot) throws ExplorerException;

    Optional<Pivot> fetchPivot(Path source, String pivotAttribute, int counter) throws ExplorerException;

    Set<String> getCompatibleFileExtensions();

}
