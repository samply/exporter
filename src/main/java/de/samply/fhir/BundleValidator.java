package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ValidationResult;
import de.samply.template.AttributeTemplate;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Resource;

public class BundleValidator {

  private FhirValidator fhirValidator;
  private Map<Resource, ValidationResult> resourceValidationResultMap = new HashMap<>();

  public BundleValidator(FhirContext fhirContext) {
    this.fhirValidator = fhirContext.newValidator();
    IValidatorModule module = new FhirInstanceValidator(fhirContext);
    fhirValidator.registerValidatorModule(module);
  }

  public boolean validate(Resource resource, AttributeTemplate attributeTemplate) {
    return validate(fetchValidationResult(resource), attributeTemplate);
  }

  private boolean validate(ValidationResult validationResult, AttributeTemplate attributeTemplate){
    return true;
  }

  private ValidationResult fetchValidationResult(Resource resource) {
    ValidationResult validationResult = resourceValidationResultMap.get(resource);
    if (validationResult == null) {
      validationResult = validateResource(resource);
      resourceValidationResultMap.put(resource, validationResult);
    }
    return validationResult;
  }

  private ValidationResult validateResource(Resource resource) {
    return this.fhirValidator.validateWithResult(resource);
  }

}
