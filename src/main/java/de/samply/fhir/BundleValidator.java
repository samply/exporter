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
  private FhirPackageLoader fhirPackageLoader;
  private ConverterTemplate converterTemplate;
  private Map<String, ValidationResult> resourceValidationResultMap = new HashMap<>();

  public BundleValidator(FhirContext fhirContext, ConverterTemplate converterTemplate,
      String fhirPackageDirectory)
      throws BundleValidatorException {
    this.fhirValidator = fhirContext.newValidator();
    this.fhirPackageLoader = new FhirPackageLoader(fhirPackageDirectory);
    this.converterTemplate = converterTemplate;
    IValidatorModule validatorModule =
        (converterTemplate.getFhirPackages() != null && !converterTemplate.getFhirPackages()
            .isEmpty()) ? fetchIValidatorModuleFromPackage(fhirContext, converterTemplate)
            : fetchIValidatorModule(fhirContext);
    fhirValidator.registerValidatorModule(validatorModule);
  }

  private IValidatorModule fetchIValidatorModule(FhirContext fhirContext) {
    return new FhirInstanceValidator(fhirContext);
  }

  private IValidatorModule fetchIValidatorModuleWithRemoteTerminologyService(
      FhirContext fhirContext, String remoteTerminologyServiceUrl, String fhirPackagesDirectory) {
    ValidationSupportChain validationSupportChain = new ValidationSupportChain();
    validationSupportChain.addValidationSupport(new DefaultProfileValidationSupport(fhirContext));
    RemoteTerminologyServiceValidationSupport remote = new RemoteTerminologyServiceValidationSupport(
        fhirContext);
    remote.setBaseUrl(remoteTerminologyServiceUrl);
    validationSupportChain.addValidationSupport(remote);
    CachingValidationSupport cache = new CachingValidationSupport(validationSupportChain);
    return new FhirInstanceValidator(cache);
  }

  private IValidatorModule fetchIValidatorModuleFromPackage(FhirContext fhirContext,
      ConverterTemplate converterTemplate)
      throws BundleValidatorException {
    ValidationSupportChain validationSupportChain = new ValidationSupportChain(
        fetchNpmPackageValidationSupport(fhirContext, converterTemplate),
        new DefaultProfileValidationSupport(fhirContext),
        new CommonCodeSystemsTerminologyService(fhirContext),
        new InMemoryTerminologyServerValidationSupport(fhirContext),
        new SnapshotGeneratingValidationSupport(fhirContext)
    );
    CachingValidationSupport validationSupport = new CachingValidationSupport(
        validationSupportChain);
    return new FhirInstanceValidator(validationSupport);
  }

  private NpmPackageValidationSupport fetchNpmPackageValidationSupport(FhirContext fhirContext,
      ConverterTemplate converterTemplate) throws BundleValidatorException {
    try {
      return fetchNpmPackageValidationSupportWithoutExceptionHandling(fhirContext,
          converterTemplate);
    } catch (RuntimeException e) {
      throw new BundleValidatorException(e);
    }
  }

  private NpmPackageValidationSupport fetchNpmPackageValidationSupportWithoutExceptionHandling(
      FhirContext fhirContext, ConverterTemplate converterTemplate) {
    NpmPackageValidationSupport npmPackageSupport = new NpmPackageValidationSupport(fhirContext);
    converterTemplate.getFhirPackages().forEach(fhirPackage -> {
      try {
        fhirPackageLoader.loadPackage(npmPackageSupport, fhirPackage);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    return npmPackageSupport;
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