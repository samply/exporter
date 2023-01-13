package de.samply.clean;

public class FilesCleanerException extends Exception {

  public FilesCleanerException() {
  }

  public FilesCleanerException(String message) {
    super(message);
  }

  public FilesCleanerException(String message, Throwable cause) {
    super(message, cause);
  }

  public FilesCleanerException(Throwable cause) {
    super(cause);
  }

  public FilesCleanerException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
