package de.samply.fhir;

public class BundleContextException extends Exception {

  public BundleContextException(String message) {
    super(message);
  }

  public BundleContextException(String message, Throwable cause) {
    super(message, cause);
  }

  public BundleContextException(Throwable cause) {
    super(cause);
  }

  public BundleContextException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
