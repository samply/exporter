package de.samply.jobs;

import de.samply.clean.FilesCleaner;
import de.samply.exporter.ExporterConst;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CleanWriteFilesJob extends FilesCleaner {

  public CleanWriteFilesJob(
      @Value(ExporterConst.WRITE_FILE_DIRECTORY_SV) String filesDirectory,
      @Value(ExporterConst.WRITE_FILES_LIFETIME_IN_DAYS_SV) Integer filesLifetimeInDays) {
    super(filesDirectory, filesLifetimeInDays);
  }

}
