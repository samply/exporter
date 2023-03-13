package de.samply.opal;

public class OpalEngineException extends Exception {

  public OpalEngineException(String message) {
    super(message);
  }

  public OpalEngineException(String message, Throwable cause) {
    super(message, cause);
  }

  public OpalEngineException(Throwable cause) {
    super(cause);
  }

  public OpalEngineException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
