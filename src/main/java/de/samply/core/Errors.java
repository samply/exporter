package de.samply.core;

public class Errors {

  private StringBuilder messages;

  public void addError(String message) {
    if (messages == null) {
      messages = new StringBuilder();
    } else {
      messages.append(" | ");
    }
    messages.append(message);
  }

  public boolean isEmpty() {
    return messages == null;
  }

  public String getMessages() {
    return (messages == null) ? "" : messages.toString();
  }

}
