package de.samply.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ConverterUtilsTest {

  @Test
  void getCombinationsAndPermutations() {
    List<List<Integer>> results = ConverterUtils.getCombinationsAndPermutations(5);
    results.forEach(integers -> {
      integers.forEach(integer -> System.out.print(integer + " "));
      System.out.println("");
    });
    //TODO
  }
}
