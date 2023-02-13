package de.samply.jobs;

import de.samply.clean.FilesCleaner;
import de.samply.exporter.ExporterConst;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CleanTempFilesJob extends FilesCleaner {

  public CleanTempFilesJob(
        @Value(ExporterConst.TEMPORAL_FILE_DIRECTORY_SV) String tempFilesDirectory,
        @Value(ExporterConst.TEMP_FILES_LIFETIME_IN_DAYS_SV) Integer tempFilesLifetimeInDays) {
      super(tempFilesDirectory, tempFilesLifetimeInDays);
  }

}
