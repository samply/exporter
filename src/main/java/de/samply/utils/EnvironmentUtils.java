package de.samply.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentUtils {

  private static Map<String, String> environmentVariables = new HashMap<>();

  public EnvironmentUtils(@Autowired Environment environment) {
    addKeyValuesFromEnvironment((ConfigurableEnvironment) environment);
  }

  public void addKeyValuesFromEnvironment(ConfigurableEnvironment environment) {
    addKeyValuesFromEnvironment(environment.getSystemEnvironment());
    addKeyValuesFromEnvironment(environment.getSystemProperties());
    addKeyValuesFromEnvironmentPropertySources(environment);
  }

  private void addKeyValuesFromEnvironment(Map<String, Object> keyValues) {
    keyValues.keySet().stream()
        .forEach(key -> environmentVariables.put(key, (String) keyValues.get(key)));
  }

  private void addKeyValuesFromEnvironmentPropertySources(ConfigurableEnvironment environment) {
    environment.getPropertySources().stream().filter(p -> p instanceof EnumerablePropertySource)
        .map(p -> ((EnumerablePropertySource) p).getPropertyNames()).flatMap(Arrays::stream)
        .distinct().forEach(key -> environmentVariables.put(key, environment.getProperty(key)));
  }

  public String getEnvironmentVariable(String key) {
    return environmentVariables.get(key);
  }

}
