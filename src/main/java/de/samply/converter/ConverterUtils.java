package de.samply.converter;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConverterUtils {

  public static List<List<Integer>> getCombinationsAndPermutations (int size){
    List<List<Integer>> results = new ArrayList<>();
    List<List<Integer>> finalResults = results;
    List<Integer> basicList = getBasicList(size);
    getCombinations(basicList).forEach(tempList -> finalResults.addAll(getPermutations(tempList)));
    results = removeDuplicates(results);

    return results;
  }

  private static List<List<Integer>> getCombinations (List<Integer> basicList){
    List<List<Integer>> results = new ArrayList<>();
    for (int i= 0; i < basicList.size(); i++){
      Set<Set<Integer>> combinations = Sets.combinations(ImmutableSet.copyOf(basicList), i + 1);
      combinations.forEach(integers -> {
        List<Integer> tempList = new ArrayList<>();
        tempList.addAll(integers);
        results.add(tempList);
      });
    }

    return results;
  }

  private static List<List<Integer>> getPermutations (List<Integer> basicList){
    List<List<Integer>> results = new ArrayList<>();
    results.addAll(Collections2.permutations(basicList));
    return results;
  }

  private static List<List<Integer>> removeDuplicates (List<List<Integer>> basicList){
    List<List<Integer>> results = new ArrayList<>();
    List<IntegerList> tempList = new ArrayList<>();
    basicList.forEach(integers -> tempList.add(new IntegerList(integers)));
    basicList.stream().distinct().forEach(integers -> results.add(integers));
    return results;
  }

  private static class IntegerList{
    private List<Integer> basicList;

    public IntegerList(List<Integer> basicList) {
      this.basicList = basicList;
    }

    @Override
    public boolean equals(Object obj) {
      List<Integer> secondList = (List<Integer>) obj;
      boolean areEquals = false;
      if (secondList.size() == basicList.size()){
        for (int i=0; i<basicList.size(); i++){
          if (basicList.get(i) != secondList.get(i)){
            return false;
          }
        }
        areEquals = true;
      }
      return areEquals;
    }
  }

  private static List<Integer> getBasicList(int size){
    List<Integer> basicList = new ArrayList<>();
    for (int i=0; i< size; i++){
      basicList.add(i);
    }
    return basicList;
  }

}
