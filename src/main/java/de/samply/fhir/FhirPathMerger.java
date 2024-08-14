package de.samply.fhir;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FhirPathMerger {

    /**
     * First of all, it parses the fhir path, so that it gives back a List<String> where every element of the list is each element of the path (parts separated by '.'). The '.' within '(' and ')' must be ignored.
     * <p>
     * Next step: It compares both List<String>. In this comparisson, the first element of both List<String> is ignored. Element after element is compared until it finds an element that is different. Here we have three scenarios:
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
     * Result of merge: Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').and(metaelement.value = 'XXX).code.value
     * <p>
     * Scenario 3:
     * The different element is a "where(...)
     * Main fhir path:      Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value
     * Secondary fhir path: Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS').code.value
     * Value: XXX
     * Result of merge: Procedure.where(category.coding.code = 'OP').outcome.where(coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS').code.value = 'XXX').coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value
     * Here, we add after the previous element of the previous element of the where, another where.
     *
     *
     * Cache Structure:
     *
     * We will use two maps:
     * Map<String, String> cache: This will store the concatenated FHIR path pair as the key and the corresponding tokenized result as the value.
     * Map<String, Integer> tokenCounterMap: This will store the concatenated FHIR path pair as the key and the counter for generating unique tokens for that specific key.
     * Token Generation:
     *
     * For each new FHIR path pair, generate a new token using the counter stored in tokenCounterMap.
     * Cache Insertion:
     *
     * Insert the merged result into the cache using the FHIR path pair as the key.
     * Increment the token counter for that specific FHIR path pair.
     *
     *
     * @param fhirPath
     * @param newFhirPath
     * @param valueAssociatedToNewFhirPath
     * @return
     */

    public static String merge(String fhirPath, String newFhirPath, String valueAssociatedToNewFhirPath) {
        //TODO
        return fhirPath;
    }


    private static final Map<String, String> cache = new HashMap<>();
    private static final Map<String, Integer> tokenCounterMap = new HashMap<>();

    public static List<String> parseFhirPath(String fhirPath) {
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

    public static String mergeFhirPaths(String mainFhirPath, String secondaryFhirPath, String value) {
        String cacheKey = mainFhirPath + "|" + secondaryFhirPath;

        // Check if the merge result is already cached
        String cachedResult = cache.get(cacheKey);
        if (cachedResult != null) {
            // Replace the token with the actual value and return the result
            return replaceTokenWithValue(cachedResult, value);
        } else {
            // Handle cache miss and try to find a matching pattern in the cache
            Optional<String> resultOptional = handleCacheMiss(mainFhirPath, secondaryFhirPath, value);
            if (resultOptional.isPresent()) {
                return resultOptional.get();
            }
            // If no matching pattern is found, perform the merge normally
            return mergeWithoutCache(mainFhirPath, secondaryFhirPath, value);
        }
    }

    private static Optional<String> handleCacheMiss(String mainFhirPath, String secondaryFhirPath, String value) {
        String cacheKey = mainFhirPath + "|" + secondaryFhirPath;

        // Check for pattern matches in the cache
        for (Map.Entry<String, String> entry : cache.entrySet()) {
            String cachedKey = entry.getKey();
            String cachedResult = entry.getValue();

            // Check if the cachedKey matches the pattern of the current key
            if (matchesPattern(cachedKey, cacheKey)) {
                // Replace the TOKEN with the value in the cached result
                String resultWithToken = cachedResult.replace("PLACEHOLDER", "'" + value + "'");
                return Optional.of(resultWithToken);
            }
        }

        // No match found
        return Optional.empty();
    }

    private static String mergeWithoutCache(String mainFhirPath, String secondaryFhirPath, String value) {
        // Correctly define cacheKey here
        String cacheKey = mainFhirPath + "|" + secondaryFhirPath;

        List<String> mainPathComponents = parseFhirPath(mainFhirPath);
        List<String> secondaryPathComponents = parseFhirPath(secondaryFhirPath);

        int firstDifferentIndex = findFirstDifferentIndex(mainPathComponents, secondaryPathComponents);

        if (firstDifferentIndex == -1) {
            return "Paths are identical or not mergable.";
        }

        String result;

        if (isWhereClause(mainPathComponents.get(firstDifferentIndex))) {
            result = handleScenario3(mainPathComponents, secondaryPathComponents, firstDifferentIndex, "PLACEHOLDER");
        } else if (firstDifferentIndex > 0 && isWhereClause(mainPathComponents.get(firstDifferentIndex - 1))) {
            result = handleScenario2(mainPathComponents, secondaryPathComponents, firstDifferentIndex, "PLACEHOLDER");
        } else {
            result = handleScenario1(mainPathComponents, secondaryPathComponents, firstDifferentIndex, "PLACEHOLDER");
        }

        // Generate a new token and cache the result
        int tokenNumber = tokenCounterMap.getOrDefault(cacheKey, 1); // Start at 1 if not present
        String token = "TOKEN" + tokenNumber;
        cache.put(cacheKey, result.replace("PLACEHOLDER", token));
        tokenCounterMap.put(cacheKey, tokenNumber + 1); // Increment the counter for this path

        // Return the result with the actual value
        return result.replace("PLACEHOLDER", "'" + value + "'");
    }

    private static int findFirstDifferentIndex(List<String> mainPathComponents, List<String> secondaryPathComponents) {
        int length = Math.min(mainPathComponents.size(), secondaryPathComponents.size());
        for (int i = 1; i < length; i++) { // Skip the first element
            if (!mainPathComponents.get(i).equals(secondaryPathComponents.get(i))) {
                return i;
            }
        }
        return -1; // No different element found
    }

    private static boolean isWhereClause(String element) {
        return element.startsWith("where(") && element.endsWith(")");
    }

    private static String handleScenario1(List<String> mainPathComponents, List<String> secondaryPathComponents, int firstDifferentIndex, String placeholder) {
        StringBuilder mergedPath = new StringBuilder();
        for (int i = 0; i < firstDifferentIndex; i++) {
            mergedPath.append(mainPathComponents.get(i)).append(".");
        }

        mergedPath.append("where(");
        for (int i = firstDifferentIndex; i < secondaryPathComponents.size(); i++) {
            mergedPath.append(secondaryPathComponents.get(i));
            if (i < secondaryPathComponents.size() - 1) {
                mergedPath.append(".");
            }
        }
        mergedPath.append(" = ").append(placeholder).append(").");

        for (int i = firstDifferentIndex; i < mainPathComponents.size(); i++) {
            mergedPath.append(mainPathComponents.get(i));
            if (i < mainPathComponents.size() - 1) {
                mergedPath.append(".");
            }
        }

        return mergedPath.toString();
    }

    private static String handleScenario2(List<String> mainPathComponents, List<String> secondaryPathComponents, int firstDifferentIndex, String placeholder) {
        StringBuilder mergedPath = new StringBuilder();
        for (int i = 0; i < firstDifferentIndex; i++) {
            mergedPath.append(mainPathComponents.get(i)).append(".");
        }

        mergedPath.append("and(");
        for (int i = firstDifferentIndex; i < secondaryPathComponents.size(); i++) {
            mergedPath.append(secondaryPathComponents.get(i));
            if (i < secondaryPathComponents.size() - 1) {
                mergedPath.append(".");
            }
        }
        mergedPath.append(" = ").append(placeholder).append(").");

        for (int i = firstDifferentIndex; i < mainPathComponents.size(); i++) {
            mergedPath.append(mainPathComponents.get(i));
            if (i < mainPathComponents.size() - 1) {
                mergedPath.append(".");
            }
        }

        return mergedPath.toString();
    }

    private static String handleScenario3(List<String> mainPathComponents, List<String> secondaryPathComponents, int firstDifferentIndex, String placeholder) {
        StringBuilder mergedPath = new StringBuilder();

        for (int i = 0; i < firstDifferentIndex; i++) {
            mergedPath.append(mainPathComponents.get(i)).append(".");
        }

        mergedPath.append("where(")
                .append(mainPathComponents.get(firstDifferentIndex - 1)).append(".")
                .append(secondaryPathComponents.get(firstDifferentIndex))
                .append(" = ")
                .append(placeholder)
                .append(").");

        for (int i = firstDifferentIndex; i < mainPathComponents.size(); i++) {
            mergedPath.append(mainPathComponents.get(i));
            if (i < mainPathComponents.size() - 1) {
                mergedPath.append(".");
            }
        }

        return mergedPath.toString();
    }

    private static String replaceTokenWithValue(String mergedPath, String value) {
        // Replace tokens with the actual value
        Pattern tokenPattern = Pattern.compile("TOKEN\\d+");
        Matcher matcher = tokenPattern.matcher(mergedPath);
        String result = mergedPath;

        while (matcher.find()) {
            String token = matcher.group();
            result = result.replace(token, "'" + value + "'");
        }

        return result;
    }

    private static boolean matchesPattern(String cachedKey, String cacheKey) {
        // Extract the patterns from cacheKey and cachedKey
        String[] cacheKeyParts = cacheKey.split("\\|");
        String[] cachedKeyParts = cachedKey.split("\\|");

        if (cachedKeyParts.length != 2) {
            return false;
        }

        String pathPart = cacheKeyParts[1];
        String cachedPattern = cachedKeyParts[1];

        // Check if the cached pattern matches the current key
        return Pattern.compile(Pattern.quote(cachedPattern).replace("TOKEN", "\\E.*?\\Q")).matcher(pathPart).matches();
    }

    public static void main(String[] args) {
        // Example 1: Test Scenario 1
        String mainFhirPath1 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value";
        String secondaryFhirPath1 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.metaelement.value";
        String result1 = mergeFhirPaths(mainFhirPath1, secondaryFhirPath1, "XXX");
        System.out.println("Result 1: " + result1);

        // Example 2: Test Scenario 2
        String mainFhirPath2 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value";
        String secondaryFhirPath2 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code2.value";
        String result2 = mergeFhirPaths(mainFhirPath2, secondaryFhirPath2, "XXX");
        System.out.println("Result 2: " + result2);

        // Example 3: Test Scenario 3
        String mainFhirPath3 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value";
        String secondaryFhirPath3 = "Procedure.where(category.coding.code = 'OP').outcome.coding"
                + ".where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS').code.value";
        String result3 = mergeFhirPaths(mainFhirPath3, secondaryFhirPath3, "XXX");
        System.out.println("Result 3: " + result3);

        // New Test 1: Simple Merge with Cache Check
        String mainFhirPath4 = "Observation.where(code.coding.code = 'BP').valueQuantity.value";
        String secondaryFhirPath4 = "Observation.where(code.coding.code = 'BP').valueQuantity.unit";
        String result4 = mergeFhirPaths(mainFhirPath4, secondaryFhirPath4, "mmHg");
        System.out.println("Result 4 (First Merge): " + result4);

        // Repeating the same merge to check if cache is used
        String result4_cached = mergeFhirPaths(mainFhirPath4, secondaryFhirPath4, "mmHg");
        System.out.println("Result 4 (Cached Merge): " + result4_cached);

        // New Test 2: Chained Merges with Cache Check
        String mainFhirPath5 = "Observation.where(code.coding.code = 'BP').valueQuantity.value";
        String secondaryFhirPath5 = "Observation.where(code.coding.code = 'BP').valueQuantity.unit";
        String result5 = mergeFhirPaths(mainFhirPath5, secondaryFhirPath5, "mmHg");
        System.out.println("Result 5: " + result5);

        // Perform another merge with the previous result as part of the next merges
        String secondaryFhirPath6 = "Observation.where(code.coding.code = 'BP').valueQuantity.comparator";
        String result6 = mergeFhirPaths(result5, secondaryFhirPath6, "<");
        System.out.println("Result 6 (Chained Merge): " + result6);

        // Check cache usage for chained merges
        String result6_cached = mergeFhirPaths(result5, secondaryFhirPath6, "<");
        System.out.println("Result 6 (Cached Chained Merge): " + result6_cached);
    }

    /*
    public static void main(String[] args) {
        List<String> fhirPath1 = parseFhirPath("Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS').code.value");
        List<String> fhirPath2 = parseFhirPath("Procedure.where(category.coding.code = 'OP').outcome.coding.where(system = 'http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS').code.value");
        System.out.println("Hello");
    }
    */


}
