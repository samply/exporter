package de.samply.fhir;


import ca.uhn.fhir.context.FhirContext;
import de.samply.container.Attribute;
import de.samply.container.Containers;
import de.samply.converter.ConverterImpl;
import de.samply.converter.Format;
import de.samply.exporter.ExporterConst;
import de.samply.logger.BufferedLoggerFactory;
import de.samply.logger.Logger;
import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;
import de.samply.template.ConverterTemplate;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.utils.FHIRPathEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
public class BundleToContainersConverter extends
        ConverterImpl<Bundle, Containers, BundleToContainersConverterSession> {

    private final static Logger logger = BufferedLoggerFactory.getLogger(BundleToContainersConverter.class);
    private final FhirContext fhirContext;
    private final FHIRPathEngine fhirPathEngine;
    private final String fhirPackagesDirectory;
    private final Boolean logFhirValidation;

    public BundleToContainersConverter(
            @Value(ExporterConst.FHIR_PACKAGES_DIRECTORY_SV) String fhirPackagesDirectory,
            @Value(ExporterConst.LOG_FHIR_VALIDATION_SV) Boolean logFhirValidation) {
        this.fhirContext = FhirContext.forR4();
        this.fhirPathEngine = createFhirPathEngine(fhirContext);
        this.fhirPackagesDirectory = fhirPackagesDirectory;
        this.logFhirValidation = logFhirValidation;
    }

    @Override
    public Flux<Containers> convert(Bundle bundle, ConverterTemplate converterTemplate,
                                    BundleToContainersConverterSession session) {
        return Flux.generate(
                sync -> {
                    logger.info("Converting bundle to containers...");
                    sync.next(convertToContainers(bundle, converterTemplate, session));
                    sync.complete();
                });
    }

    @Override
    protected BundleToContainersConverterSession initializeSession(ConverterTemplate template) {
        return new BundleToContainersConverterSession();
    }

    public Containers convertToContainers(Bundle bundle, ConverterTemplate converterTemplate,
                                          BundleToContainersConverterSession session) {
        Containers containers = new Containers();
        BundleContext context = createBundleContext(bundle, converterTemplate, session);
        if (converterTemplate != null) {
            converterTemplate.getContainerTemplates()
                    .forEach(containerTemplate -> addContainers(bundle, containers, containerTemplate,
                            context));
        }
        return containers;
    }

    private BundleContext createBundleContext(Bundle bundle, ConverterTemplate converterTemplate,
                                              BundleToContainersConverterSession session) {
        try {
            return new BundleContext(bundle, session, fhirPathEngine, fhirContext, converterTemplate,
                    fhirPackagesDirectory, logFhirValidation);
        } catch (BundleContextException e) {
            throw new RuntimeException(e);
        }
    }


    private void addContainers(Bundle bundle, Containers containers,
                               ContainerTemplate containerTemplate, BundleContext context) {
        logger.debug("Adding container " + fetchContainerName(containerTemplate) + "...");
        containerTemplate.getAttributeTemplates().forEach(attributeTemplate ->
                bundle.getEntry().forEach(bundleEntryComponent -> {
                    if (isSameResourceType(bundleEntryComponent.getResource(), attributeTemplate)) {
                        fetchResourceAttribute(bundleEntryComponent.getResource(), containerTemplate,
                                attributeTemplate, context).forEach(resourceAttribute ->
                                addResourceAttributeToContainers(containers, resourceAttribute, context));
                    }
                }));
    }

    private String fetchContainerName(ContainerTemplate containerTemplate) {
        if (containerTemplate.getCsvFilename() != null) {
            return containerTemplate.getCsvFilename();
        }
        if (containerTemplate.getExcelSheet() != null) {
            return containerTemplate.getExcelSheet();
        }
        if (containerTemplate.getJsonFilename() != null) {
            return containerTemplate.getJsonFilename();
        }
        return "";
    }

    private boolean isSameResourceType(Resource resource, AttributeTemplate attributeTemplate) {
        return
                (attributeTemplate.getJoinFhirPath() != null && attributeTemplate.isDirectJoinFhirPath() &&
                        isSameResourceType(resource,
                                AttributeTemplate.removeChildFhirPathHead(attributeTemplate.getJoinFhirPath()))) ||
                        isSameResourceType(resource, attributeTemplate.getValFhirPath());
    }

    private boolean isSameResourceType(Resource resource, String fhirPath) {
        String resourceType = resource.getMeta().getProfile().get(0).getValue();
        if (resourceType.contains("/")) {
            resourceType = resourceType.substring(resourceType.lastIndexOf('/') + 1);
        }
        if (fhirPath.contains(".")) {
            fhirPath = fhirPath.substring(0, fhirPath.indexOf("."));
        }
        return resourceType.toLowerCase().contains(fhirPath.toLowerCase());
    }

    private List<ResourceAttribute> fetchResourceAttribute(Resource resource,
                                                           ContainerTemplate containerTemplate, AttributeTemplate attributeTemplate,
                                                           BundleContext context) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        context.fetchRelatedResources(resource, attributeTemplate).forEach(relatedResource -> {
            ExpressionNode expressionNode = fhirPathEngine.parse(attributeTemplate.getValFhirPath());
            Resource evalResource =
                    (attributeTemplate.isDirectChildFhirPath()) ? resource : relatedResource;
            Resource idResource =
                    (attributeTemplate.isDirectChildFhirPath()) ? relatedResource : resource;
            if (evalResource == null || idResource == null) {
                logErrorByFetchingResourceAttribute(resource, relatedResource, evalResource, idResource, containerTemplate, attributeTemplate);
            } else if (isToBeEvaluated(evalResource, idResource, attributeTemplate)) {
                fhirPathEngine.evaluate(evalResource, expressionNode)
                        .forEach(base -> resourceAttributes.add(
                                new ResourceAttribute(idResource,
                                        fetchAttributeValue(evalResource, attributeTemplate, base, context),
                                        containerTemplate, attributeTemplate)));
            }
        });
        return resourceAttributes;
    }

    private void logErrorByFetchingResourceAttribute(Resource resource, Resource relatedResource, Resource evalResource, Resource idResource, ContainerTemplate containerTemplate, AttributeTemplate attributeTemplate) {
        logger.error("Error fetching ressource attribute for container " + containerTemplate.getCsvFilename() + " and attribute " + attributeTemplate.getCsvColumnName());
        logResource("Resource: ", resource);
        logResource("Related Resource: ", relatedResource);
        logResource("Eval Resource: ", evalResource);
        logResource("Id Resource: ", idResource);
    }

    private void logResource(String initialMessage, Resource resource) {
        String resourceId = (resource != null) ? fetchResourceId(resource) : "not found";
        logger.error(initialMessage + resourceId);
    }

    private String fetchResourceId(Resource resource) {
        return resource.getResourceType() + "/" + resource.getIdPart();
    }

    private String fetchAttributeValue(Resource evalResource, AttributeTemplate attributeTemplate,
                                       Base base, BundleContext bundleContext) {
        return (attributeTemplate.isValidation()) ?
                bundleContext.validate(evalResource, attributeTemplate) : base.toString();
    }

    private boolean isToBeEvaluated(Resource evalResource, Resource idResource,
                                    AttributeTemplate attributeTemplate) {
        boolean result = true;
        if (attributeTemplate.getConditionValueFhirPath() != null) {
            result = evaluateCondition(evalResource, attributeTemplate.getConditionValueFhirPath());
        }
        if (attributeTemplate.getConditionIdFhirPath() != null) {
            result = result && evaluateCondition(idResource, attributeTemplate.getConditionIdFhirPath());
        }
        return result;
    }

    private boolean evaluateCondition(Resource resource, String conditionFhirPath) {
        ExpressionNode expression = fhirPathEngine.parse(conditionFhirPath);
        List<Base> baseList = fhirPathEngine.evaluate(resource, expression);
        if (baseList.size() > 0) {
            Base base = baseList.get(0);
            if (base instanceof BooleanType) {
                return ((BooleanType) base).booleanValue();
            }
        }
        return true;
    }

    private void addResourceAttributeToContainers(Containers containers,
                                                  ResourceAttribute resourceAttribute, BundleContext context) {
        containers.addAttribute(resourceAttribute.containerTemplate(),
                resourceAttribute.fetchContainerId(),
                new Attribute(resourceAttribute.attributeTemplate(),
                        fetchResourceAttributeValue(resourceAttribute, context)));
    }

    private String fetchResourceAttributeValue(ResourceAttribute resourceAttribute,
                                               BundleContext context) {
        String result = resourceAttribute.attributeValue();
        if (resourceAttribute.attributeTemplate().getOperation() != null) {
            result = resourceAttribute.attributeTemplate().getOperation().execute(result);
        }
        if (resourceAttribute.attributeTemplate().getAnonym() != null) {
            result = context.fetchAnonym(resourceAttribute.attributeTemplate(), result);
        }
        return result;
    }

    private FHIRPathEngine createFhirPathEngine(FhirContext fhirContext) {
        return new FHIRPathEngine(
                new HapiWorkerContext(fhirContext, fhirContext.getValidationSupport()));
    }

    @Override
    public Format getInputFormat() {
        return Format.BUNDLE;
    }

    @Override
    public Format getOutputFormat() {
        return Format.CONTAINERS;
    }

}
