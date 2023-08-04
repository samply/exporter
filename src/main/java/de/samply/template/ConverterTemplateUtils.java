package de.samply.template;

import de.samply.exporter.ExporterConst;
import de.samply.utils.EnvironmentUtils;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConverterTemplateUtils {

  private EnvironmentUtils environmentUtils;
  private String timestampFormat;

  public ConverterTemplateUtils(
      @Value(ExporterConst.TIMESTAMP_FORMAT_SV) String timestampFormat,
      @Autowired EnvironmentUtils environmentUtils) {
    this.timestampFormat = timestampFormat;
    this.environmentUtils = environmentUtils;
  }

  public String replaceTokens(String originalText) {

    AtomicReference<String> textReference = new AtomicReference<>(originalText);
    if (containsVariable(originalText)) {
      // Replace predefined tokens
      Arrays.stream(ContainerToken.values()).forEach(containerToken -> {
        if (textReference.get().contains(containerToken.name())) {
          textReference.set(new TokenReplacer(containerToken, environmentUtils, timestampFormat,
              textReference.get()).getTokenReplacer());
        }
      });
      // If is not a predefined token, replace through environment variable.
      while (containsVariable(textReference.get())) {
        textReference.set(
            new TokenReplacer(environmentUtils, textReference.get()).getTokenReplacer());
      }
    }

    return textReference.get();
  }

  public boolean containsVariable(String text) {
    return text.contains(ExporterConst.TOKEN_HEAD) && text.contains(ExporterConst.TOKEN_END);
  }

}
