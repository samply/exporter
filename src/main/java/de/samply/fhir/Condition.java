package de.samply.fhir;

import java.util.List;
import java.util.Optional;

/**
 * TODO: Limitation: We only consider operators and values. Functions are set in the element, but currently
 * syntactically and semantically ignored.
 * A description of this functions can be found in https://pathling.csiro.au/docs/fhirpath/functions
 *
 * @param element
 * @param op
 * @param value
 */
public record Condition(String element, String op, String value) {

    // TODO: Limitation: Currently operators are limited to the following list:
    private static final List<String> OPERATORS = List.of("=", "!=", ">", "<", ">=", "<=");

    @Override
    public String toString() {
        return element + ((op != null && op.length() > 0 && value != null && value.length() > 0) ?
                " " + op + " " + value : "");
    }

    public static Optional<Condition> of(String condition) {
        if (condition != null && condition.length() > 0) {
            // Normalize spaces
            String trimmedInput = condition.trim();

            // Iterate through known operators to find one in the string
            int operatorIndex = -1;
            String op = "";
            // Get the last operator index;
            for (String operator : OPERATORS) {
                int tempIndex = trimmedInput.lastIndexOf(operator);
                if (tempIndex > operatorIndex){
                    operatorIndex = tempIndex;
                    op = operator;
                }
            }

            if (operatorIndex != -1) {
                // Split input into element, operator, and value
                String element = trimmedInput.substring(0, operatorIndex).trim();
                String value = trimmedInput.substring(operatorIndex + op.length()).trim();
                return Optional.of(new Condition(element, op, value));
            }

            // If no operator is found, treat the entire input as the element
            return Optional.of(new Condition(trimmedInput, null, null));

        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        Optional<Condition> condition1 = of("e1.e2 = 'v'");  // Expected: ("e1.e2", "=", "'v'")
        Optional<Condition> condition2 = of("function()");   // Expected: ("function()", null, null)
        Optional<Condition> condition3 = of("XXX");          // Expected: ("XXX", null, null)
        Optional<Condition> condition4 = of("X1 X2");        // Expected: ("X1 X2", null, null)
        Optional<Condition> condition5 = of("X1(X2 X3) X4 X5"); // Expected: ("X1(X2 X3) X4 X5", null, null)
        Optional<Condition> condition6 = of("X1(X2 X3) > X5"); // Expected: ("X1(X2 X3)", ">", "X5")
    }
}
