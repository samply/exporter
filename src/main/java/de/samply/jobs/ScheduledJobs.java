package de.samply.jobs;

import de.samply.clean.FilesCleanerException;
import de.samply.exporter.ExporterConst;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobs {

  private final static Logger logger = BufferedLoggerFactory.getLogger(ScheduledJobs.class);
  private CleanTempFilesJob cleanTempFilesJob;
  private CleanWriteFilesJob cleanWriteFilesJob;
  private QueryArchiverJob queryArchiverJob;


  public ScheduledJobs(CleanTempFilesJob cleanTempFilesJob, CleanWriteFilesJob cleanWriteFilesJob, QueryArchiverJob queryArchiverJob) {
    this.cleanTempFilesJob = cleanTempFilesJob;
    this.cleanWriteFilesJob = cleanWriteFilesJob;
    this.queryArchiverJob = queryArchiverJob;
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

  @Scheduled(cron = ExporterConst.ARCHIVE_EXPIRED_QUERIES_CRON_EXPRESSION_SV)
  public void archiveQueries() throws FilesCleanerException {
    logger.info("Archiving expired queries");
    queryArchiverJob.archiveQueries();
  }

}
