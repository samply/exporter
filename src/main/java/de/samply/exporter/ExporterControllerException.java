package de.samply.exporter;

public class ExporterControllerException extends Exception {

  public ExporterControllerException() {
  }

  public ExporterControllerException(String message) {
    super(message);
  }

  public ExporterControllerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExporterControllerException(Throwable cause) {
    super(cause);
  }

  public ExporterControllerException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
