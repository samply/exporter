package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ValidationResult;
import de.samply.template.AttributeTemplate;
import de.samply.template.ConverterTemplate;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.RemoteTerminologyServiceValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleValidator {

  private final static Logger logger = LoggerFactory.getLogger(BundleValidator.class);
  private FhirValidator fhirValidator;
  private ConverterTemplate converterTemplate;
  private Map<String, ValidationResult> resourceValidationResultMap = new HashMap<>();

  public BundleValidator(FhirContext fhirContext, ConverterTemplate converterTemplate)
      throws BundleValidatorException {
    this.fhirValidator = fhirContext.newValidator();
    this.converterTemplate = converterTemplate;
    IValidatorModule validatorModule = (converterTemplate.getFhirProfileUrl() != null) ?
        fetchIValidatorModuleFromPackageWithoutExceptionHandling(fhirContext,
            converterTemplate.getFhirProfileUrl()) : fetchIValidatorModule(fhirContext);
    fhirValidator.registerValidatorModule(validatorModule);
  }

  private IValidatorModule fetchIValidatorModule(FhirContext fhirContext) {
    return new FhirInstanceValidator(fhirContext);
  }

  private IValidatorModule fetchIValidatorModuleWithRemoteTerminologyService(
      FhirContext fhirContext, String remoteTerminologyServiceUrl) {
    ValidationSupportChain validationSupportChain = new ValidationSupportChain();
    validationSupportChain.addValidationSupport(new DefaultProfileValidationSupport(fhirContext));
    RemoteTerminologyServiceValidationSupport remote = new RemoteTerminologyServiceValidationSupport(
        fhirContext);
    remote.setBaseUrl(remoteTerminologyServiceUrl);
    validationSupportChain.addValidationSupport(remote);
    CachingValidationSupport cache = new CachingValidationSupport(validationSupportChain);
    return new FhirInstanceValidator(cache);
  }


  private IValidatorModule fetchIValidatorModuleFromPackageWithoutExceptionHandling(
      FhirContext fhirContext, String remoteTerminologyServiceUrl) throws BundleValidatorException {
    try {
      return fetchIValidatorModuleFromPackage(fhirContext, remoteTerminologyServiceUrl);
    } catch (IOException e) {
      throw new BundleValidatorException(e);
    }
  }

  private IValidatorModule fetchIValidatorModuleFromPackage(
      FhirContext fhirContext, String remoteTerminologyServiceUrl) throws IOException {
    NpmPackageValidationSupport npmPackageSupport = new NpmPackageValidationSupport(fhirContext);
    npmPackageSupport.loadPackageFromClasspath(createFhirPackagePath("hl7.fhir.core-4.0.1.tgz"));
    npmPackageSupport.loadPackageFromClasspath(createFhirPackagePath("hl7.fhir.r4.core-4.0.1.tgz"));
    npmPackageSupport.loadPackageFromClasspath(
        createFhirPackagePath("de.basisprofil.r4-1.4.0.tgz"));
    npmPackageSupport.loadPackageFromClasspath(createFhirPackagePath(remoteTerminologyServiceUrl));
    ValidationSupportChain validationSupportChain = new ValidationSupportChain(
        npmPackageSupport,
        new DefaultProfileValidationSupport(fhirContext),
        new CommonCodeSystemsTerminologyService(fhirContext),
        new InMemoryTerminologyServerValidationSupport(fhirContext),
        new SnapshotGeneratingValidationSupport(fhirContext)
    );
    CachingValidationSupport validationSupport = new CachingValidationSupport(
        validationSupportChain);
    return new FhirInstanceValidator(validationSupport);
  }

  private String createFhirPackagePath(String fhirPackage) {
    return "fhir-packages/" + fhirPackage;
  }

  public String validate(Resource resource, AttributeTemplate attributeTemplate) {
    try {
      return validate(fetchValidationResult(resource), attributeTemplate);
    } catch (Exception e) {
      logger.error(ExceptionUtils.getStackTrace(e));
      return e.getMessage();
    }
  }

  private String validate(ValidationResult validationResult, AttributeTemplate attributeTemplate) {
    return (validationResult != null && !validationResult.isSuccessful())
        ? ValidationUtils.fetchAsOneMessage(validationResult, attributeTemplate, converterTemplate)
        : "";
  }

  private ValidationResult fetchValidationResult(Resource resource) {
    ValidationResult validationResult = resourceValidationResultMap.get(getResourceId(resource));
    if (validationResult == null) {
      validationResult = validateResource(resource);
      if (validationResult != null && !validationResult.isSuccessful()) {
        validationResult.getMessages()
            .forEach(singleValidationMessage -> logger.error(singleValidationMessage.toString()));
      }
      resourceValidationResultMap.put(getResourceId(resource), validationResult);
    }
    return validationResult;
  }

  private String getResourceId(Resource resource) {
    return resource.getId();
  }

  private ValidationResult validateResource(Resource resource) {
    return this.fhirValidator.validateWithResult(resource);
  }

}
