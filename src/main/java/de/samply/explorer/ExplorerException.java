package de.samply.explorer;

public class ExplorerException extends Exception {

  public ExplorerException(String message) {
    super(message);
  }

  public ExplorerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExplorerException(Throwable cause) {
    super(cause);
  }

  public ExplorerException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
