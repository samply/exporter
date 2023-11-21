package de.samply.utils;

import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


class QueryContextUtilsTest {

    @Test
    void fetchKeyValuesFromContext() {
        String test1 = "KEY1=VALUE1; KEY2= VALUE2; KEY3= \"This is value 32\"  ;KEY4 = VALUE4";
        String encodedTest1 = Base64.getEncoder().encodeToString(test1.getBytes());
        Map<String, String> result = QueryContextUtils.fetchKeyValuesFromContext(encodedTest1);
        assertEquals(result.size(), 4);
        assertEquals(result.get("KEY1"), "VALUE1");
        assertEquals(result.get("KEY2"), "VALUE2");
        assertEquals(result.get("KEY3"), "This is value 32");
        assertEquals(result.get("KEY4"), "VALUE4");
    }
}
