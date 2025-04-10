package de.samply.template;

import de.samply.exporter.ExporterConst;
import de.samply.template.token.TokenContext;
import de.samply.template.token.TokenReplacer;
import de.samply.utils.EnvironmentUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ConverterTemplateUtils {

    private final EnvironmentUtils environmentUtils;
    private final String timestampFormat;
    private final String site;

    public ConverterTemplateUtils(
            @Value(ExporterConst.TIMESTAMP_FORMAT_SV) String timestampFormat,
            @Value(ExporterConst.SITE_SV) String site,
            EnvironmentUtils environmentUtils) {
        this.timestampFormat = timestampFormat;
        this.environmentUtils = environmentUtils;
        this.site = site;
    }

    public String replaceTokens(String originalText) {
        return replaceTokens(originalText, new TokenContext(environmentUtils));
    }

    public String replaceTokens(String originalText, TokenContext tokenContext) {

        AtomicReference<String> textReference = new AtomicReference<>(originalText);
        if (containsVariable(originalText)) {
            // Replace predefined tokens
            Arrays.stream(ContainerToken.values()).forEach(containerToken -> {
                if (textReference.get().contains(containerToken.name())) {
                    textReference.set(
                            new TokenReplacer(containerToken, timestampFormat, site, tokenContext, textReference.get()).getTokenReplacer());
                }
            });
            // If is not a predefined token, replace through environment variable.
            while (containsVariable(textReference.get())) {
                textReference.set(new TokenReplacer(tokenContext, textReference.get()).getTokenReplacer());
            }
        }

        return textReference.get();
    }

    public boolean containsVariable(String text) {
        return text.contains(ExporterConst.TOKEN_HEAD) && text.contains(ExporterConst.TOKEN_END);
    }

}
