package de.samply.fhir;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FhirPathMerger {


    /**
     * First of all, it parses the fhir path, so that it gives back a List<String> where every element of the list is each element of the path (parts separated by '.'). The '.' within '(' and ')' must be ignored.
     * <p>
     * Next step: It compares both List<String>. In this comparison, the first element of both List<String> is ignored. Element after element is compared until it finds an element that is different. Here we have three scenarios:
     * <p>
     * Scenario 1.:
     * The different element is not a "where(..." and there is not a "where(...)" preceding that element:
     * <p>
     * An example would be:
     * Main fhir path:      Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value
     * Secondary fhir path: Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.metaelement.value
     * Value: XXX
     * Result of merge: Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.where(metaelement.value = 'XXX).value
     * <p>
     * Here we build the where taking the rest of the List<String> after the first different element: "metaelement.value"
     * <p>
     * Scenario 2:
     * The different element is not a "where(..." and there is a "where(...)" preceding that element:
     * <p>
     * Main fhir path:      Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value
     * Secondary fhir path: Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code2.value
     * Value: XXX
     * Result of merge: Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS' and metaelement.value = 'XXX).code.value
     * <p>
     * Scenario 3:
     * The different element is a "where(...)
     * Main fhir path:      Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value
     * Secondary fhir path: Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS').code.value
     * Value: XXX
     * Result of merge: Procedure.where(category.coding.code = 'OP').outcome.where(coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS').code.value = 'XXX').coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value
     * Here, we add after the previous element of the previous element of the where, another where.
     * <p>
     * <p>
     * Cache Structure:
     * <p>
     * We will use two maps:
     * Map<String, String> cache: This will store the concatenated FHIR path pair as the key and the corresponding tokenized result as the value.
     * Map<String, Integer> tokenCounterMap: This will store the concatenated FHIR path pair as the key and the counter for generating unique tokens for that specific key.
     * Token Generation:
     * <p>
     * For each new FHIR path pair, generate a new token using the counter stored in tokenCounterMap.
     * Cache Insertion:
     * <p>
     * Insert the merged result into the cache using the FHIR path pair as the key.
     * Increment the token counter for that specific FHIR path pair.
     *
     * @param mainFhirPath
     * @param newFhirPath
     * @param valueAssociatedToNewFhirPath
     * @return
     */
    public static String merge(String mainFhirPath, String newFhirPath, String valueAssociatedToNewFhirPath) {
        FhirPath mainPath = createFhirPath(mainFhirPath);
        FhirPath newPath = createFhirPath(newFhirPath);
        FhirPath mergedPath = new FhirPath();
        AtomicInteger index = new AtomicInteger(0);
        AtomicBoolean newPathConditionAlreadySet = new AtomicBoolean(false);
        mainPath.getElements().forEach(element -> {
            if (newPath.getElements().size() > index.get() && !newPathConditionAlreadySet.get() && !areEqualNodes(mainPath, newPath, index.get())) {
                newPath.createConditionForFhirPathAfterIndexElement(index.get(), valueAssociatedToNewFhirPath).ifPresent(condition -> mergedPath.addConditions(Set.of(condition)));
                newPathConditionAlreadySet.set(true);
            }
            mergedPath.addElement(element);
            mainPath.getWhereClauseConditions(index.get()).ifPresent(mergedPath::addConditions);
            if (!newPathConditionAlreadySet.get()) {
                // Add also new Path conditions if they are not set
                newPath.getWhereClauseConditions(index.get()).ifPresent(mergedPath::addConditions);
            }
            index.getAndIncrement();
        });
        return mergedPath.flatten();
    }

    private static boolean areEqualNodes(FhirPath mainPath, FhirPath secondaryPath, int index) {
        if (index < 0 || index >= mainPath.getElements().size() || index >= secondaryPath.getElements().size() ||
                !mainPath.getElements().get(index).equals(secondaryPath.getElements().get(index))
        ) {
            return false;
        } else {
            return areCompatibleConditions(mainPath, secondaryPath, index);
        }
    }

    private static boolean areCompatibleConditions(FhirPath mainPath, FhirPath secondaryPath, int index) {
        Optional<Set<Condition>> mainPathConditions = mainPath.getWhereClauseConditions(index);
        Optional<Set<Condition>> secondaryPathConditions = secondaryPath.getWhereClauseConditions(index);
        return (mainPathConditions.isEmpty() && secondaryPathConditions.isEmpty() || // If there are no conditions
                mainPathConditions.isPresent() && secondaryPathConditions.isEmpty() || // If only the main has conditions
                mainPathConditions.isEmpty() && secondaryPathConditions.isPresent() || // If only the secondary has conditions
                mainPathConditions.isPresent() && secondaryPathConditions.isPresent() &&
                        areCompatibleConditions(mainPathConditions.get(), secondaryPathConditions.get())
        );
    }

    private static boolean areCompatibleConditions(Set<Condition> conditions1, Set<Condition> conditions2) {
        for (Condition condition1 : conditions1) {
            for (Condition condition2 : conditions2) {
                if (condition1.element().equals(condition2.element()) && !condition1.equals(condition2)) {
                    // TODO: Limitation: We don't consider merging ">".
                    //  For example, if an element > 1 and element > 2 should be compatible, but it is not our case.
                    // If a condition has the same element but different operator or value, the conditions are not compatible
                    return false;
                }
            }
        }
        return true;
    }

    private static FhirPath createFhirPath(String fhirPath) {
        return new FhirPath(parseFhirPath(fhirPath));
    }

    private static List<String> parseFhirPath(String fhirPath) {
        List<String> components = new ArrayList<>();
        StringBuilder currentComponent = new StringBuilder();
        int parenthesesDepth = 0;

        for (int i = 0; i < fhirPath.length(); i++) {
            char c = fhirPath.charAt(i);

            if (c == '(') {
                parenthesesDepth++;
                currentComponent.append(c);
            } else if (c == ')') {
                parenthesesDepth--;
                currentComponent.append(c);
            } else if (c == '.' && parenthesesDepth == 0) {
                components.add(currentComponent.toString().trim());
                currentComponent.setLength(0);
            } else {
                currentComponent.append(c);
            }
        }

        if (currentComponent.length() > 0) {
            components.add(currentComponent.toString().trim());
        }

        return components;
    }


    public static void main(String[] args) {
        // Example 1: Test Scenario 1
        String mainFhirPath1 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value";
        String secondaryFhirPath1 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.metaelement.value";
        String result1 = merge(mainFhirPath1, secondaryFhirPath1, "XXX");
        System.out.println("Result 1: " + result1);

        // Example 2: Test Scenario 2
        String mainFhirPath2 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value";
        String secondaryFhirPath2 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code2.value";
        String result2 = merge(mainFhirPath2, secondaryFhirPath2, "XXX");
        System.out.println("Result 2: " + result2);

        // Example 3: Test Scenario 3
        String mainFhirPath3 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value";
        String secondaryFhirPath3 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS').code.value";
        String result3 = merge(mainFhirPath3, secondaryFhirPath3, "XXX");
        System.out.println("Result 3: " + result3);

        String mainFhirPath4 = "MedicationStatement.medication.coding.version.value";
        String secondaryFhirPath4 = "MedicationStatement.medication.text.value";
        String result4 = merge(mainFhirPath4, secondaryFhirPath4, "Carboplatin-Text Gemcitabin-Text");
        System.out.println("Result 4: " + result4);

        String mainFhirPath5 = "MedicationStatement.medication.where(text.value = 'Carboplatin-Text Gemcitabin-Text' ).coding.version.value";
        String secondaryFhirPath5 = "MedicationStatement.medication.coding.code.value";
        String result5 = merge(mainFhirPath5, secondaryFhirPath5, "L01BC05");
        System.out.println("Result 5: " + result5);

    }


}
