package de.samply.clean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesCleaner {

  private final static Logger logger = LoggerFactory.getLogger(FilesCleaner.class);
  private Path filesDirectory;
  private Duration filesLifetimeInDays;

  public FilesCleaner(String filesDirectory, Integer filesLifetimeInDays) {
    this.filesDirectory = Path.of(filesDirectory);
    this.filesLifetimeInDays = Duration.ofDays(filesLifetimeInDays);
  }

  public void clean() throws FilesCleanerException {
    try {
      cleanWithoutExceptionManagement();
    } catch (IOException | RuntimeException e) {
      throw new FilesCleanerException(e);
    }
  }

  private void cleanWithoutExceptionManagement() throws IOException {
    Files.list(filesDirectory).filter(path -> !Files.isDirectory(path))
        .forEach(path -> {
          try {
            cleanPath(path);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  private void cleanPath(Path path) throws IOException {
    Instant lastModifiedTime = getLastModifiedTime(path);
    Instant currentTime = Instant.now();
    Duration duration = Duration.between(lastModifiedTime, currentTime);
    if (duration.compareTo(filesLifetimeInDays) >= 0) {
      logger.info("Deleting file " + path.toAbsolutePath().toString());
      Files.deleteIfExists(path);
    }
  }

  private Instant getLastModifiedTime(Path path) throws IOException {
    BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
    return basicFileAttributes.lastModifiedTime().toInstant();
  }

}
