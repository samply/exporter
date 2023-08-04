package de.samply.explorer;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public interface Explorer {

    Path filter(Path source, Pivot[] pivots) throws ExplorerException;

    Optional<Pivot[]> fetchPivot(Path source, String pivotAttribute, int pageCounter, int pageSize) throws ExplorerException;

    Set<String> getCompatibleFileExtensions();

    int fetchTotalNumberOfElements (Path source) throws ExplorerException;

}
