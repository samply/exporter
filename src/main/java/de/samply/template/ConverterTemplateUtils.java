package de.samply.template;

import de.samply.teiler.TeilerConst;
import de.samply.utils.EnvironmentUtils;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConverterTemplateUtils {

  private EnvironmentUtils environmentUtils;

  public ConverterTemplateUtils(@Autowired EnvironmentUtils environmentUtils) {
    this.environmentUtils = environmentUtils;
  }

  public String replaceTokens(String originalText) {

    AtomicReference<String> textReference = new AtomicReference<>(originalText);
    if (containsVariable(originalText)) {
      // Replace predefined tokens
      Arrays.stream(ContainerToken.values()).forEach(containerToken -> {
        if (textReference.get().contains(containerToken.name())) {
          textReference.set(new TokenReplacer(containerToken, environmentUtils,
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
    return text.contains(TeilerConst.TOKEN_HEAD) && text.contains(TeilerConst.TOKEN_END);
  }

}
