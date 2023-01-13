package de.samply.teiler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class TeilerConfig {

  @Bean
  public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
    propertySourcesPlaceholderConfigurer.setLocations(
        new ClassPathResource("application.properties"));
    propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
    return propertySourcesPlaceholderConfigurer;
  }

}
