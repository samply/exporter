package de.samply.fhir;

import jakarta.validation.constraints.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FhirPath {
    private List<String> elements = new ArrayList<>();
    private Map<Integer, Set<Condition>> elementIndexWhereClauseMap = new HashMap<>();

    public FhirPath() {
    }

    public FhirPath(List<String> fhirPath) {
        AtomicInteger index = new AtomicInteger(-1);
        fhirPath.forEach(element -> {
            if (isWhereClause(element)) {
                if (index.get() < 0) {
                    throw new RuntimeException("Fhir path is wrong built: " + fetchPlainFhirPath(elements));
                }
                elementIndexWhereClauseMap.put(index.get(), parseWhereClause(element));
            } else {
                elements.add(element);
                index.getAndIncrement();
            }
        });
    }

    public List<String> getElements() {
        return elements;
    }

    public Optional<Set<Condition>> getWhereClauseConditions(Integer index) {
        return Optional.ofNullable(elementIndexWhereClauseMap.get(index));
    }

    public void addElement(String element) {
        elements.add(element);
    }

    public void addConditions(Set<Condition> conditions) {
        addConditions(conditions, 0);
    }

    public void addConditions(Set<Condition> conditions, int differenceIndex) {
        int index = elements.size() - 1 + differenceIndex;
        if (index < 0) {
            index = 0;
        } else if (index > elements.size() - 1) {
            index = elements.size() - 1;
        }
        Set<Condition> currentConditions = elementIndexWhereClauseMap.get(index);
        if (currentConditions == null) {
            currentConditions = new HashSet<>();
        }
        currentConditions.addAll(conditions);
        elementIndexWhereClauseMap.put(index, currentConditions);
    }

    private static boolean isWhereClause(String element) {
        return element.startsWith("where(") && element.endsWith(")");
    }

    private static String removeWhereIfPresent(String condition) {
        return (condition.startsWith("where(") && condition.endsWith(")")) ?
                condition.substring("where(".length(), condition.length() - 2) : condition;
    }

    private static String fetchPlainFhirPath(List<String> elements) {
        StringBuilder plainFhirPath = new StringBuilder("");
        elements.forEach(e -> plainFhirPath.append(e).append('.'));
        if (plainFhirPath.length() > 0) {
            plainFhirPath.deleteCharAt(plainFhirPath.length() - 1);
        }
        return plainFhirPath.toString();
    }

    public String flatten() {
        StringBuilder result = new StringBuilder();
        AtomicInteger index = new AtomicInteger(0);
        elements.forEach(element -> {
            result.append(element).append('.');
            getWhereClauseConditions(index.getAndIncrement()).ifPresent(conditions ->
                    flattenConditionsAsWhereClause(conditions).ifPresent(whereClause -> result.append(whereClause).append(".")));
        });
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1); // Remove last '.'
        }
        return result.toString();
    }

    private Optional<String> flattenConditionsAsWhereClause(@NotNull Set<Condition> conditions) {
        if (conditions != null && !conditions.isEmpty()) {
            StringBuilder result = new StringBuilder("where(");
            conditions.forEach(condition -> result.append(condition.toString()).append(" and "));
            result.delete(result.length() - " and ".length(), result.length()) // Remove las " and "
                    .append(")");
            return Optional.of(result.toString());
        }
        return Optional.empty();
    }

    // This method generates a condition equaling to "value" the fhir path starting from the element at "index"
    public Optional<Condition> createConditionForFhirPathAfterIndexElement(int index, String value) {
        StringBuilder conditionElement = new StringBuilder();
        if (index >= 0 && index < elements.size()) {
            for (int i = index; i < elements.size(); i++) {
                conditionElement.append(elements.get(i)).append('.');
                mergeConditionsInWhereClause(i).ifPresent(whereClause -> conditionElement.append(whereClause).append('.'));
            }
            if (conditionElement.length() > 0) {
                conditionElement.deleteCharAt(conditionElement.length() - 1); // Delete last '.'
                // TODO: Limitation: Currently, we only support "=" as operator. Extend it to other operators
                return Optional.of(new Condition(conditionElement.toString(), "=", "'" + escapeSpecialCharacters(value) + "'"));
            }
        }
        return Optional.empty();
    }

    // Based on escape characters of method 'processConstant' of org.hl7.fhir.r4.utils.FHIRLexer.java

    /**
     * Replaces all single backslashes in the input string with double backslashes,
     * ensuring that backslashes which are already part of a pair (i.e., `\\`) are
     * not affected. This method is useful for cases where lone backslashes need
     * to be escaped, but double backslashes should remain unchanged.
     * <p>
     * The method works by using a regular expression to match single backslashes
     * that are not immediately preceded or followed by another backslash, and then
     * replaces them with two backslashes.
     *
     * <p><b>Pattern Explanation:</b></p>
     * <ul>
     *     <li><code>(?&lt;!\\\\)</code> - Negative lookbehind: Ensures that there is no backslash
     *     before the current backslash.</li>
     *     <li><code>\\\\</code> - Matches a literal backslash (`\`) in the string.</li>
     *     <li><code>(?!\\\\)</code> - Negative lookahead: Ensures that there is no backslash
     *     after the current backslash.</li>
     * </ul>
     * This pattern matches a single backslash that is not part of a pair.
     * <p>
     * The replacement string <code>"\\\\\\\\"</code> adds two backslashes in the place
     * of the matched single backslash. In Java, backslashes must be escaped, so the
     * replacement string consists of four pairs of backslashes, which the Java compiler
     * interprets as two actual backslashes (`\\`) in the final result.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String input = "This \\ is a test \\\\ string \\with \\\\multiple \\backslashes";
     * String result = input.replaceAll("(?<!\\\\)\\\\(?!\\\\)", "\\\\\\\\");
     * System.out.println(result);
     * // Output: This \\ is a test \\ string \\with \\multiple \\backslashes
     * }</pre>
     */
    private String escapeSpecialCharacters(String value) {
        if (value != null) {
            value = value
                    .replaceAll("(?<!\\\\)\\\\(?!\\\\)", "\\\\\\\\")
                    .replace("\t", "\\\t")
                    .replace("\r", "\\\r")
                    .replace("\n", "\\\n")
                    .replace("\f", "\\\f")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("`", "\\`")
                    .replace("/", "\\/");
        }
        return value;
    }

    private Optional<String> mergeConditionsInWhereClause(int index) {
        Set<Condition> conditions = elementIndexWhereClauseMap.get(index);
        if (conditions != null && conditions.size() > 0) {
            StringBuilder result = new StringBuilder("where(");
            conditions.forEach(condition -> result.append(condition.toString()).append(" and "));
            result.delete(result.length() - " and ".length(), result.length()) // Remove last and
                    .append(")");
            return Optional.of(result.toString());
        }
        return Optional.empty();
    }

    private static Set<Condition> parseWhereClause(String expression) {
        // Remove the "where(" prefix and the final ')'
        if (expression.startsWith("where(") && expression.endsWith(")")) {
            expression = expression.substring("where(".length(), expression.length() - 1);
        }
        return parseConditions(expression);
    }

    private static Set<Condition> parseConditions(String expression) {
        Set<Condition> conditions = new HashSet<>();
        StringBuilder currentCondition = new StringBuilder();
        int nestedLevel = 0;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (c == '(') {
                nestedLevel++;
                currentCondition.append(c);
            } else if (c == ')') {
                nestedLevel--;
                currentCondition.append(c);
            } else if (nestedLevel == 0 && expression.startsWith(" and ", i)) {
                // Split the condition at the top level when encountering "and"
                Condition.of(currentCondition.toString()).ifPresent(conditions::add);
                currentCondition.setLength(0); // Reset for the next condition
                i += 4; // Skip past the " and "
            } else {
                currentCondition.append(c);
            }
        }

        // Add the last condition if there is any
        if (currentCondition.length() > 0) {
            Condition.of(currentCondition.toString()).ifPresent(conditions::add);
        }

        return conditions;
    }

    public static void main(String[] args) {
        Set<Condition> result1 = parseWhereClause("");
        Set<Condition> result2 = parseWhereClause("where(e1.e2 = 'v1')");
        Set<Condition> result3 = parseWhereClause("where(e1.e2 = 'v1' and e3.e4 = 'v2')");
        Set<Condition> result4 = parseWhereClause("where(e1.e2 = 'v1'  and    e3.e4 = 'v2'   )");
        Set<Condition> result5 = parseWhereClause("where(e1.e2 = 'v1'  and    e3.e4 = 'v2' and e5.where(e6.e7 = 'v8' and e9.where(e10.e11.e12 = 'v3').e13 = 'v4').e15 = 'v6'  )");
        Set<Condition> result6 = parseWhereClause("where(e1.e2.e3 = 'v1' and e1.where(e4.e5 = 'v4' and e6.where(e12.e13 = 'v17').e8 = 'v6').e9.e10 = 'v9')");
    }

}
