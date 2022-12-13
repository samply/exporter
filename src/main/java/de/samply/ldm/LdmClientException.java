package de.samply.ldm;

public class LdmClientException extends Exception{

  public LdmClientException() {
  }

  public LdmClientException(String message) {
    super(message);
  }

  public LdmClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public LdmClientException(Throwable cause) {
    super(cause);
  }

}
