package de.samply;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

public class EnvironmentTestUtils {

  public static ConfigurableEnvironment getEmptyMockEnvironment(){
    return getMockEnvironment(new HashMap<>());
  }

  public static ConfigurableEnvironment getMockEnvironment(Map<String, Object> properties) {
    MutablePropertySources propertySources = mock(MutablePropertySources.class);
    ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
    when(propertySources.stream()).thenReturn(Stream.empty());
    when(environment.getPropertySources()).thenReturn(propertySources);
    when(environment.getSystemEnvironment()).thenReturn(new HashMap<>());
    when(environment.getSystemProperties()).thenReturn(properties);
    return environment;
  }


}
