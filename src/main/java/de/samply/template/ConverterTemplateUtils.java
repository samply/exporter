package de.samply.template;

import de.samply.teiler.TeilerConst;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class ConverterTemplateUtils {

  public static String replaceTokens(String originalText) {

    AtomicReference<String> textReference = new AtomicReference<>(originalText);
    Arrays.stream(ContainerToken.values()).forEach(containerToken -> {
      if (textReference.get().contains(containerToken.name())) {
        textReference.set(new TokenReplacer(containerToken, originalText).getTokenReplacer());
      }
    });
    return textReference.get();
  }

  private static class TokenReplacer {

    private ContainerToken containerToken;
    private String text;
    private String extension;
    private int headIndex;
    private int endIndex;

    public TokenReplacer(ContainerToken containerToken, String text) {
      this.containerToken = containerToken;
      this.text = text;
      this.headIndex = text.indexOf(TeilerConst.TOKEN_HEAD + containerToken.name());
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
      return switch (containerToken) {
        case TIMESTAMP -> getTimestamp(extension);
      };
    }

  }

  private static String getTimestamp(String format) {
    if (format == null) {
      format = TeilerConst.DEFAULT_TIMESTAMP_FORMAT;
    }
    return new SimpleDateFormat(format).format(Timestamp.from(Instant.now()));
  }

}
