package de.samply.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.template.AttributeTemplate;
import de.samply.template.ConverterTemplate;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BundleValidator {

    private final static Logger logger = BufferedLoggerFactory.getLogger(BundleValidator.class);
    private FhirValidator fhirValidator;
    private FhirPackageLoader fhirPackageLoader;
    private ConverterTemplate converterTemplate;
    private Map<String, ValidationResult> resourceValidationResultMap = new HashMap<>();
    private ValidationResult emptyValidationResult = new ValidationResult(null, new ArrayList<>());
    private final boolean logFhirValidation;

    public BundleValidator(FhirContext fhirContext, ConverterTemplate converterTemplate,
                           String fhirPackageDirectory, boolean logFhirValidation)
            throws BundleValidatorException {
        this.fhirValidator = fhirContext.newValidator();
        this.fhirPackageLoader = new FhirPackageLoader(fhirPackageDirectory);
        this.converterTemplate = converterTemplate;
        IValidatorModule validatorModule =
                (converterTemplate.getFhirPackages() != null && !converterTemplate.getFhirPackages()
                        .isEmpty()) ? fetchIValidatorModuleFromPackageAndRemoteTerminologyServers(fhirContext,
                        converterTemplate)
                        : fetchIValidatorModule(fhirContext);
        fhirValidator.registerValidatorModule(validatorModule);
        this.logFhirValidation = logFhirValidation;
    }

    private IValidatorModule fetchIValidatorModule(FhirContext fhirContext) {
        return new FhirInstanceValidator(fhirContext);
    }

    private IValidatorModule fetchIValidatorModuleFromPackageAndRemoteTerminologyServers(
            FhirContext fhirContext, ConverterTemplate converterTemplate)
            throws BundleValidatorException {
        ValidationSupportChain validationSupportChain = new ValidationSupportChain(
                fetchNpmPackageValidationSupport(fhirContext, converterTemplate),
                new DefaultProfileValidationSupport(fhirContext),
                new CommonCodeSystemsTerminologyService(fhirContext),
                new InMemoryTerminologyServerValidationSupport(fhirContext),
                new SnapshotGeneratingValidationSupport(fhirContext)
        );
        addRemoteTerminologyServers(validationSupportChain, fhirContext, converterTemplate);
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

    private void addRemoteTerminologyServers(ValidationSupportChain validationSupportChain,
                                             FhirContext fhirContext, ConverterTemplate template) {
        template.getFhirTerminologyServers().forEach(fhirTerminologyServer -> {
            RemoteTerminologyServiceValidationSupport remote = new RemoteTerminologyServiceValidationSupport(
                    fhirContext);
            remote.setBaseUrl(fhirTerminologyServer);
            validationSupportChain.addValidationSupport(remote);
        });
    }

    public String validate(Resource resource, AttributeTemplate attributeTemplate) {
        try {
            return validate(fetchValidationResult(resource, attributeTemplate), attributeTemplate);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String validate(ValidationResult validationResult, AttributeTemplate attributeTemplate) {
        return (validationResult != null && !validationResult.isSuccessful())
                ? ValidationUtils.fetchAsOneMessage(validationResult, attributeTemplate, converterTemplate)
                : "";
    }

    private ValidationResult fetchValidationResult(Resource resource, AttributeTemplate template) {
        ValidationResult validationResult = resourceValidationResultMap.get(getResourceId(resource));
        if (validationResult == null) {
            validationResult = validateResource(resource);
            if (logFhirValidation && validationResult != null && !validationResult.isSuccessful()) {
                validationResult.getMessages()
                        .forEach(
                                singleValidationMessage ->
                                        logValidationMessage(singleValidationMessage, template, resource));
            }
            if (validationResult == null) {
                validationResult = emptyValidationResult;
            }
            resourceValidationResultMap.put(getResourceId(resource), validationResult);
        }
        return (validationResult != emptyValidationResult) ? validationResult : null;
    }

    private void logValidationMessage(SingleValidationMessage message, AttributeTemplate template,
                                      Resource resource) {
        String attribute =
                (template.getCsvColumnName() != null) ? template.getCsvColumnName() + ": " : "";
        logger.info(attribute + resource.getId() + ": " + message.toString());
    }

    private String getResourceId(Resource resource) {
        return resource.getId();
    }

    private ValidationResult validateResource(Resource resource) {
        return this.fhirValidator.validateWithResult(resource);
    }

}
