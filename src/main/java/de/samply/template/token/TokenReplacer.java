package de.samply.template.token;

import de.samply.exporter.ExporterConst;
import de.samply.template.ContainerToken;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

public class TokenReplacer {

    private ContainerToken containerToken;
    private String timestampFormat;
    private String text;
    private String extension;
    private int headIndex;
    private int endIndex;
    private TokenContext tokenContext;

    public TokenReplacer(ContainerToken containerToken,
                         String timestampFormat, TokenContext tokenContext, String text) {
        this(tokenContext, text);
        this.containerToken = containerToken;
        this.timestampFormat = timestampFormat;
        this.headIndex = text.indexOf(ExporterConst.TOKEN_HEAD + containerToken.name());
        this.endIndex = headIndex + text.substring(headIndex).indexOf(ExporterConst.TOKEN_END);
    }

    public TokenReplacer(TokenContext tokenContext, String text) {
        this.text = text;
        this.tokenContext = tokenContext;
        this.headIndex = text.indexOf(ExporterConst.TOKEN_HEAD);
        this.endIndex = text.indexOf(ExporterConst.TOKEN_END);
        if (text.contains(ExporterConst.TOKEN_EXTENSION_DELIMITER)) {
            this.extension = text.substring(text.indexOf(ExporterConst.TOKEN_EXTENSION_DELIMITER) + 1,
                    text.indexOf(ExporterConst.TOKEN_END));
        }
    }

    public String getTokenReplacer() {
        return text.substring(0, headIndex) + getTokenReplacingValue() + (
                (endIndex > -1 && endIndex < text.length()) ? text.substring(endIndex + 1) : "");
    }

    private String getTokenReplacingValue() {
        // Replace container token
        if (containerToken != null) {
            return switch (containerToken) {
                case TIMESTAMP -> getTimestamp(extension);
            };
        }
        // Replace variable from keyValues
        String result = getVariableValue();
        return result.trim();
    }

    private String getVariableValue() {
        return getVariableValue(tokenContext::getValue);
    }

    private String getVariableValue(Function<String, Optional<String>> function) {
        Optional<String> result = function.apply(text.substring(headIndex + ExporterConst.TOKEN_HEAD.length(), endIndex));
        return (result.isPresent()) ? result.get() : "";
    }

    private String getTimestamp(String format) {
        if (format == null) {
            format = timestampFormat;
        }
        return new SimpleDateFormat(format).format(Timestamp.from(Instant.now()));
    }

    public static String removeTokens(String csvFilename) {
        boolean hasVariable = true;
        while (hasVariable) {
            int index = csvFilename.indexOf(ExporterConst.TOKEN_HEAD);
            if (index == -1) {
                hasVariable = false;
            } else {
                int index2 = csvFilename.substring(index + ExporterConst.TOKEN_HEAD.length())
                        .indexOf(ExporterConst.TOKEN_END);
                if (index2 == -1) {
                    hasVariable = false;
                } else {
                    csvFilename = csvFilename.substring(0, index) + csvFilename.substring(
                            index + ExporterConst.TOKEN_HEAD.length() + index2 + 1);
                }
            }
        }
        return csvFilename;
    }

    public static boolean isSameElement(String elementWithReplacedTokens,
                                        String elementWithoutTokens) {
        for (int i = 0, j = 0;
             i < elementWithoutTokens.length() && j < elementWithReplacedTokens.length(); i++, j++) {
            while (elementWithoutTokens.charAt(i) != elementWithReplacedTokens.charAt(j)) {
                j++;
                if (j == elementWithReplacedTokens.length()) {
                    return false;
                }
            }
            if (i + 1 == elementWithoutTokens.length()) {
                return true;
            }
        }
        return false;
    }

}
