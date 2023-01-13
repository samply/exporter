package de.samply.zip;

public class ZipperException extends Exception {

  public ZipperException() {
  }

  public ZipperException(String message) {
    super(message);
  }

  public ZipperException(String message, Throwable cause) {
    super(message, cause);
  }

  public ZipperException(Throwable cause) {
    super(cause);
  }

  public ZipperException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
