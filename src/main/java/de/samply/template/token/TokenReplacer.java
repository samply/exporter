package de.samply.template.token;

import de.samply.exporter.ExporterConst;
import de.samply.template.ContainerToken;
import de.samply.utils.EnvironmentUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TokenReplacer {

    private Map<String, String> keyValues;
    private EnvironmentUtils environmentUtils;
    private ContainerToken containerToken;
    private String timestampFormat;
    private String text;
    private String extension;
    private int headIndex;
    private int endIndex;

    public TokenReplacer(ContainerToken containerToken, EnvironmentUtils environmentUtils,
                         String timestampFormat, String text) {
        this(environmentUtils, text);
        this.containerToken = containerToken;
        this.timestampFormat = timestampFormat;
        this.headIndex = text.indexOf(ExporterConst.TOKEN_HEAD + containerToken.name());
        this.endIndex = headIndex + text.substring(headIndex).indexOf(ExporterConst.TOKEN_END);
    }

    public TokenReplacer(EnvironmentUtils environmentUtils, String text) {
        this(null, environmentUtils, text);
    }

    public TokenReplacer(HttpServletRequest request, EnvironmentUtils environmentUtils, String text) {
        this.text = text;
        this.environmentUtils = environmentUtils;
        this.keyValues = fetchParametersFromHttpServletRequest(request);
        this.headIndex = text.indexOf(ExporterConst.TOKEN_HEAD);
        this.endIndex = text.indexOf(ExporterConst.TOKEN_END);
        if (text.contains(ExporterConst.TOKEN_EXTENSION_DELIMITER)) {
            this.extension = text.substring(text.indexOf(ExporterConst.TOKEN_EXTENSION_DELIMITER) + 1,
                    text.indexOf(ExporterConst.TOKEN_END));
        }
    }

    private Map<String, String> fetchParametersFromHttpServletRequest(HttpServletRequest httpServletRequest){
        Map<String, String> result = new HashMap<>();
        if (httpServletRequest != null){
            httpServletRequest.getParameterMap().keySet().forEach(key ->{
                String[] values = httpServletRequest.getParameterMap().get(key);
                if (values != null && values.length >= 1){
                    result.put(key, values[0]);
                }
            });
        }
        return result;
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
        String result = getVariableValueFromKeyValues();
        if (result.trim().length() == 0) {
            // Replace variable from environment variables
            result = getEnvironmentVariable();
        }
        return result;
    }

    private String getVariableValueFromKeyValues() {
        return getVariableValue(keyValues::get);
    }

    private String getEnvironmentVariable() {
        return getVariableValue(environmentUtils::getEnvironmentVariable);
    }

    private String getVariableValue(Function<String, String> function) {
        String result = function.apply(
                text.substring(headIndex + ExporterConst.TOKEN_HEAD.length(), endIndex));
        return (result != null) ? result : "";
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
