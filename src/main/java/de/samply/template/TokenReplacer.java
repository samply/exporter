package de.samply.template;

import de.samply.teiler.TeilerConst;
import de.samply.utils.EnvironmentUtils;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class TokenReplacer {

  private EnvironmentUtils environmentUtils;
  private ContainerToken containerToken;
  private String text;
  private String extension;
  private int headIndex;
  private int endIndex;

  public TokenReplacer(ContainerToken containerToken, EnvironmentUtils environmentUtils,
      String text) {
    this(environmentUtils, text);
    this.containerToken = containerToken;
    this.headIndex = text.indexOf(TeilerConst.TOKEN_HEAD + containerToken.name());
    this.endIndex = headIndex + text.substring(headIndex).indexOf(TeilerConst.TOKEN_END);
  }

  public TokenReplacer(EnvironmentUtils environmentUtils, String text) {
    this.text = text;
    this.environmentUtils = environmentUtils;
    this.headIndex = text.indexOf(TeilerConst.TOKEN_HEAD);
    this.endIndex = text.indexOf(TeilerConst.TOKEN_END);
    if (text.contains(TeilerConst.TOKEN_EXTENSION_DELIMITER)) {
      this.extension = text.substring(text.indexOf(TeilerConst.TOKEN_EXTENSION_DELIMITER) + 1,
          text.indexOf(TeilerConst.TOKEN_END));
    }
  }


  public String getTokenReplacer() {
    return text.substring(0, headIndex) + getTokenReplacingValue() + (
        (endIndex > -1 && endIndex < text.length()) ? text.substring(endIndex + 1) : "");
  }

  private String getTokenReplacingValue() {
    return (containerToken == null) ? getEnvironmentVariable() :
        switch (containerToken) {
          case TIMESTAMP -> getTimestamp(extension);
        };
  }

  private String getEnvironmentVariable() {
    String result = environmentUtils.getEnvironmentVariable(
        text.substring(headIndex + TeilerConst.TOKEN_HEAD.length(), endIndex));
    return (result != null) ? result : "";
  }

  private String getTimestamp(String format) {
    if (format == null) {
      format = TeilerConst.DEFAULT_TIMESTAMP_FORMAT;
    }
    return new SimpleDateFormat(format).format(Timestamp.from(Instant.now()));
  }


}
