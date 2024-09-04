package de.samply.fhir;

import de.samply.template.AttributeTemplate;
import de.samply.template.ContainerTemplate;

import java.util.*;

public class ContainerAttributesFhirDependencies {

    private final Map<AttributeTemplate, List<AttributeTemplate>> attributeDependencies;

    public ContainerAttributesFhirDependencies(ContainerTemplate containerTemplate) {
        Map<AttributeTemplate, List<String>> attributeTemplatePathMap = new HashMap<>();
        containerTemplate.getAttributeTemplates().forEach(attributeTemplate ->
                attributeTemplatePathMap.put(attributeTemplate, fetchFhirSearchPathNodes(attributeTemplate)));
        attributeDependencies = findSharedPaths(attributeTemplatePathMap);
    }

    public List<AttributeTemplate> getFhirDependencies(AttributeTemplate attributeTemplate) {
        return attributeDependencies.get(attributeTemplate);
    }

    private static Map<AttributeTemplate, List<AttributeTemplate>> findSharedPaths(Map<AttributeTemplate, List<String>> attributeTemplatePathMap) {
        Map<AttributeTemplate, List<AttributeTemplate>> sharedPaths = new HashMap<>();
        attributeTemplatePathMap.keySet().forEach(attributeTemplate -> {
            List<String> path1 = attributeTemplatePathMap.get(attributeTemplate);
            List<AttributeTemplate> sharedWith = new ArrayList<>();
            attributeTemplatePathMap.keySet().stream().filter(attributeTemplate2 -> attributeTemplate2 != attributeTemplate).forEach(attributeTemplate2 -> {
                List<String> path2 = attributeTemplatePathMap.get(attributeTemplate2);
                if (hasCommonPart(path1, path2)) {
                    sharedWith.add(attributeTemplate2);
                }
            });
            sharedPaths.put(attributeTemplate, sharedWith);
        });
        return sharedPaths;
    }

    // Two paths are the same if the first two elements are the same
    private static boolean hasCommonPart(List<String> path1, List<String> path2) {
        return path1.size() > 1 && path2.size() > 1 && path1.get(0).equals(path2.get(0)) && path1.get(1).equals(path2.get(1));
    }

    /**
     * This method generates a list of elements of the fhir search path. If there is a where, it is added to the previous node. If the first node is the root, where is ignored
     * <p>
     * For example: If the attribute template has only val-fhir-path="Procedure.extension('http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp').value.code.value",
     * it will generate a list of Strings with the elements
     * {"ROOT", "extension('http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp')", "value", "code", "value"}
     * <p>
     * If there is a direct join:
     * val-fhir-path="Observation.where(code.coding.code = 'LA4583-6').effective.value" and join-fhir-path="ClinicalImpression.finding.itemReference.reference.value"
     * it will generate:
     * {"ROOT", "finding", "itemReference", "reference", "value", "Observation.where(code.coding.code = 'LA4583-6')", "effective", "value"}
     * <p>
     * If There is an indirect join fhir path:
     * val-fhir-path="AdverseEvent.severity.coding.code.value" and join-fhir-path="/AdverseEvent.suspectEntity.instance.reference.where(value.startsWith('Procedure')).value"
     * it will generate:
     * {"ROOT", "AdverseEvent", "severity", "coding", "code", "value"}
     * Here, the join-fhir-path is ignored because the main resource (ROOT) references directly this resource.
     * <p>
     * Another example with where:
     * val-fhir-path = "Observation.where(code.coding.code = 'LA4583-6').effective.value"
     * -> {"ROOT", "effective", "value"}
     *
     * @param attributeTemplate Attribute Template.
     * @return List of Strings with the nodes of the fhir search path.
     */
    private static List<String> fetchFhirSearchPathNodes(AttributeTemplate attributeTemplate) {
        return mergeWhereElements(splitByDotIgnoringParentheses(attributeTemplate.getValFhirPath()));
    }

    private static List<String> mergeWhereElements(List<String> list) {
        List<String> mergedList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            String current = list.get(i);

            if (current.startsWith("where")) {
                if (i > 0) {
                    String previous = mergedList.remove(mergedList.size() - 1);
                    mergedList.add(previous + "." + current);
                }
            } else {
                mergedList.add(current);
            }
        }

        return mergedList;
    }

    // This method splits the fhir search path by dot, ignoring parenthesis
    private static List<String> splitByDotIgnoringParentheses(String fhirSearchPath) {
        List<String> result = new ArrayList<>();
        if (fhirSearchPath != null && fhirSearchPath.length() > 0) {
            Stack<Character> stack = new Stack<>();
            StringBuilder current = new StringBuilder();

            for (char ch : fhirSearchPath.toCharArray()) {
                if (ch == '(') {
                    stack.push(ch);
                    current.append(ch);
                } else if (ch == ')') {
                    if (!stack.isEmpty() && stack.peek() == '(') {
                        stack.pop();
                    }
                    current.append(ch);
                } else if (ch == '.' && stack.isEmpty()) {
                    result.add(current.toString());
                    current.setLength(0); // Clear the current StringBuilder
                } else {
                    current.append(ch);
                }
            }
            // Add the last part
            if (current.length() > 0) {
                result.add(current.toString());
            }
        }
        return result;
    }

    public static void main(String[] args) {
        AttributeTemplate attributeTemplate = new AttributeTemplate();
        attributeTemplate.setValFhirPath("Procedure.extension('http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp').value.code.value");
        List<String> nodes1 = fetchFhirSearchPathNodes(attributeTemplate);

        attributeTemplate = new AttributeTemplate();
        attributeTemplate.setJoinFhirPath("/AdverseEvent.suspectEntity.instance.reference.where(value.startsWith('Procedure')).value");
        attributeTemplate.setValFhirPath("AdverseEvent.severity.coding.code.value");
        List<String> nodes2 = fetchFhirSearchPathNodes(attributeTemplate);

        attributeTemplate = new AttributeTemplate();
        attributeTemplate.setJoinFhirPath("ClinicalImpression.finding.itemReference.reference.value");
        attributeTemplate.setValFhirPath("Observation.where(code.coding.code = 'LA4583-6').effective.value");
        List<String> nodes3 = fetchFhirSearchPathNodes(attributeTemplate);

        attributeTemplate = new AttributeTemplate();
        attributeTemplate.setValFhirPath("Observation.where(code.coding.code = 'LA4583-6').effective.value");
        List<String> nodes4 = fetchFhirSearchPathNodes(attributeTemplate);

        System.out.println("Hello");
    }

}
