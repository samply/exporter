package de.samply.fhir;

import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.samply.exporter.ExporterConst;
import de.samply.template.AttributeTemplate;
import de.samply.template.ConverterTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ValidationUtils {

  public static String fetchAsOneMessage(ValidationResult validationResult,
      AttributeTemplate attributeTemplate, ConverterTemplate converterTemplate) {
    AtomicReference<String> result = new AtomicReference<>("");
    Arrays.stream(fetchMessages(validationResult, attributeTemplate, converterTemplate)).forEach(
        message -> result.set(result.get() + message + ExporterConst.VALIDATION_MESSAGE_SEPARATOR));
    int index = result.get().lastIndexOf(ExporterConst.VALIDATION_MESSAGE_SEPARATOR);
    return (index >= 0) ? result.get().substring(0, index) : result.get();
  }

  public static String[] fetchMessages(ValidationResult validationResult,
      AttributeTemplate attributeTemplate, ConverterTemplate converterTemplate) {
    List<String> result = new ArrayList<>();
    validationResult.getMessages().forEach(singleValidationMessage -> {
      if (isMessageForAttributeTemplate(singleValidationMessage, attributeTemplate)) {
        result.add(
            singleValidationMessage.getMessage().replace(converterTemplate.getCsvSeparator(), " "));
      }
    });
    return result.toArray(new String[0]);
  }

  private static boolean isMessageForAttributeTemplate(
      SingleValidationMessage singleValidationMessage,
      AttributeTemplate attributeTemplate) {
    boolean result = isMatch(attributeTemplate.getValFhirPath(),
        singleValidationMessage.getLocationString());
    if (!result) {
      return result;
    }
    Optional<String> fhirPathInMessage = extractFhirPathInMessage(singleValidationMessage);
    return fhirPathInMessage.isPresent() ?
        ValidationUtils.isMatch(attributeTemplate.getValFhirPath(), fhirPathInMessage.get())
        : true;
  }

  private static Optional<String> extractFhirPathInMessage(
      SingleValidationMessage singleValidationMessage) {
    int index = singleValidationMessage.getLocationString().indexOf(".");
    String fhirRootElement =
        (index > 0) ? singleValidationMessage.getLocationString().substring(0, index)
            : singleValidationMessage.getLocationString();
    String message = singleValidationMessage.getMessage();
    for (String part : message.split(" ")) {
      if (part.contains(fhirRootElement) && part.contains(".")) {
        int lastIndex = part.lastIndexOf(".");
        // Remove dot at the end of the string
        if (lastIndex == part.length() - 1) {
          part = part.substring(0, lastIndex);
        }
        if (part.contains(".")) {
          part = part.replace(":", "");
          part = part.replace(",", "");
          return Optional.of(part);
        }
      }
    }
    return Optional.empty();
  }

  private static boolean isMatch(String fhirPath1, String fhirPath2) {
    String simplifiedFhirPath1 = removeSpecialBlocks(fhirPath1);
    String simplifiedFhirPath2 = removeSpecialBlocks(fhirPath2);
    String[] fhirPathGroup1 = {fhirPath1, simplifiedFhirPath1};
    String[] fhirPathGroup2 = {fhirPath2, simplifiedFhirPath2};
    return (isMatch(fhirPathGroup1, fhirPathGroup2) || isMatch(fhirPathGroup2, fhirPathGroup1));
  }

  private static boolean isMatch(String[] fhirPathGroup1, String[] fhirPathGroup2) {
    for (String fhirPath1 : fhirPathGroup1) {
      for (String fhirPath2 : fhirPathGroup2) {
        if (fhirPath1.contains(fhirPath2)) {
          return true;
        }
      }
    }
    return false;
  }

  private static String removeSpecialBlocks(String input) {
    input = removeBlock(input, "[", "]");
    input = removeBlock(input, "where(", ")");
    return input;
  }

  private static String removeBlock(String input, String blockHead, String blockEnd) {
    String result = input;
    while (result.contains(blockHead)) {
      int index1 = result.indexOf(blockHead);
      if (index1 >= 0) {
        int index2 = result.indexOf(blockEnd);
        String tempResult = result.substring(0, index1);
        if (index2 < result.length() - 1) {
          tempResult += result.substring(index2 + 1);
        }
        result = tempResult;
      }
    }
    return result;
  }

}
