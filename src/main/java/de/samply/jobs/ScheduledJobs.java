package de.samply.jobs;

import de.samply.clean.FilesCleanerException;
import de.samply.exporter.ExporterConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobs {

  private final static Logger logger = LoggerFactory.getLogger(ScheduledJobs.class);
  private CleanTempFilesJob cleanTempFilesJob;
  private CleanWriteFilesJob cleanWriteFilesJob;


  public ScheduledJobs(
      @Autowired CleanTempFilesJob cleanTempFilesJob,
      @Autowired CleanWriteFilesJob cleanWriteFilesJob) {
    this.cleanTempFilesJob = cleanTempFilesJob;
    this.cleanWriteFilesJob = cleanWriteFilesJob;
  }

  @Scheduled(cron = ExporterConst.CLEAN_TEMP_FILES_CRON_EXPRESSION_SV)
  public void cleanTempFiles() throws FilesCleanerException {
    logger.info("Cleaning temporal files");
    cleanTempFilesJob.clean();
  }

  @Scheduled(cron = ExporterConst.CLEAN_WRITE_FILES_CRON_EXPRESSION_SV)
  public void cleanWriteFiles() throws FilesCleanerException {
    logger.info("Cleaning write files");
    cleanWriteFilesJob.clean();
  }

}
