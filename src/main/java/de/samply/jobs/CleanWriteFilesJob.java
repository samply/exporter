package de.samply.jobs;

import de.samply.clean.FilesCleaner;
import de.samply.teiler.TeilerConst;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CleanWriteFilesJob extends FilesCleaner {

  public CleanWriteFilesJob(
      @Value(TeilerConst.WRITE_FILE_DIRECTORY_SV) String filesDirectory,
      @Value(TeilerConst.WRITE_FILES_LIFETIME_IN_DAYS_SV) Integer filesLifetimeInDays) {
    super(filesDirectory, filesLifetimeInDays);
  }

}
