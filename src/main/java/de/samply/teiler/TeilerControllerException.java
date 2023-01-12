package de.samply.teiler;

public class TeilerControllerException extends Exception {

  public TeilerControllerException() {
  }

  public TeilerControllerException(String message) {
    super(message);
  }

  public TeilerControllerException(String message, Throwable cause) {
    super(message, cause);
  }

  public TeilerControllerException(Throwable cause) {
    super(cause);
  }

  public TeilerControllerException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
