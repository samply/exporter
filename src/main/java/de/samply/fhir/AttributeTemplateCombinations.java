package de.samply.fhir;

import de.samply.template.AttributeTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeTemplateCombinations {

    private final int maxNumberOfAttributes;
    private final Map<Integer, List<List<AttributeTemplate>>> numberOfAttributesXattributesCombinationsMap;

    public AttributeTemplateCombinations(List<AttributeTemplate> attributes) {
        numberOfAttributesXattributesCombinationsMap = generateGroupedCombinations(attributes);
        maxNumberOfAttributes = attributes.size();
    }

    public List<List<AttributeTemplate>> getCombinations(int numberOfAttributes) {
        return (numberOfAttributes > 0 && numberOfAttributes <= maxNumberOfAttributes) ?
                numberOfAttributesXattributesCombinationsMap.get(numberOfAttributes) : new ArrayList<>();
    }

    private static Map<Integer, List<List<AttributeTemplate>>> generateGroupedCombinations(List<AttributeTemplate> attributes) {
        Map<Integer, List<List<AttributeTemplate>>> result = new HashMap<>();

        // Generate combinations for each possible size (from the full list to individual elements)
        for (int i = attributes.size(); i > 0; i--) {
            List<List<AttributeTemplate>> combinationsOfSize = new ArrayList<>();
            combine(attributes, i, 0, new ArrayList<>(), combinationsOfSize);
            result.put(i, combinationsOfSize);
        }
        return result;
    }

    private static void combine(List<AttributeTemplate> attributes, int combinationSize, int startIndex, List<AttributeTemplate> currentCombination, List<List<AttributeTemplate>> result) {
        // Base case: if the current combination has the desired size, add it to the result
        if (currentCombination.size() == combinationSize) {
            result.add(new ArrayList<>(currentCombination));
            return;
        }

        // Recursive case: generate combinations
        for (int i = startIndex; i < attributes.size(); i++) {
            currentCombination.add(attributes.get(i));
            combine(attributes, combinationSize, i + 1, currentCombination, result);
            currentCombination.remove(currentCombination.size() - 1); // backtrack
        }
    }
}
