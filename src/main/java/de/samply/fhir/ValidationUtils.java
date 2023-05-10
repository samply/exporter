package de.samply.fhir;

import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.samply.exporter.ExporterConst;
import de.samply.template.AttributeTemplate;
import de.samply.template.ConverterTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        result.add(singleValidationMessage.getMessage().replace(converterTemplate.getCsvSeparator(), " "));
      }
    });
    return result.toArray(new String[0]);
  }

  private static boolean isMessageForAttributeTemplate(
      SingleValidationMessage singleValidationMessage,
      AttributeTemplate attributeTemplate) {
    String valFhirPath = attributeTemplate.getValFhirPath();
    String valFhirPath2 = removeSpecialBlocks(valFhirPath);
    String locationString = singleValidationMessage.getLocationString();
    String locationString2 = removeSpecialBlocks(locationString);
    return valFhirPath.contains(locationString)
        || valFhirPath.contains(locationString2)
        || valFhirPath2.contains(locationString)
        || valFhirPath2.contains(locationString2)
        || locationString.contains(valFhirPath)
        || locationString.contains(valFhirPath2)
        || locationString2.contains(valFhirPath)
        || locationString2.contains(valFhirPath2);
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
