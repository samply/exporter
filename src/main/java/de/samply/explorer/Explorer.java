package de.samply.explorer;

import java.nio.file.Path;
import java.util.Optional;

public interface Explorer {

  Path filter (Path source, Pivot pivot) throws ExplorerException;
  Optional<Pivot> fetchPivot (Path source, String pivotAttribute, int counter) throws ExplorerException;

}
