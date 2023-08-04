package de.samply.fhir;

public class BundleValidatorException extends Exception {

  public BundleValidatorException(String message) {
    super(message);
  }

  public BundleValidatorException(String message, Throwable cause) {
    super(message, cause);
  }

  public BundleValidatorException(Throwable cause) {
    super(cause);
  }

  public BundleValidatorException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
