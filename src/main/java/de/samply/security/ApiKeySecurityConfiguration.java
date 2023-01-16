package de.samply.security;


import de.samply.teiler.TeilerConst;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


/**
 * Configuration of Spring Boot Security.
 */
@Configuration
@EnableWebSecurity
@Order(1)
public class ApiKeySecurityConfiguration {

  private ApiKeyAuthenticationManager apiKeyAuthenticationManager;


  /**
   * Add API key filter to Spring http security.
   *
   * @param httpSecurity Spring http security.
   * @return Security Filter Chain based on apiKey.
   * @throws Exception Exception.
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .cors(Customizer.withDefaults())
        .securityMatcher(TeilerConst.REST_PATHS_WITH_API_KEY)
        .csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .addFilter(createApiKeyFilter())
        .authorizeHttpRequests()
        .anyRequest()
        .authenticated();

    return httpSecurity.build();

  }

  @Autowired
  public void setApiKeyAuthenticationManager(
      ApiKeyAuthenticationManager apiKeyAuthenticationManager) {
    this.apiKeyAuthenticationManager = apiKeyAuthenticationManager;
  }

  @Bean
  public ApiKeyFilter createApiKeyFilter() {

    ApiKeyFilter apiKeyFilter = new ApiKeyFilter();
    apiKeyFilter.setAuthenticationManager(apiKeyAuthenticationManager);
    return apiKeyFilter;

  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value(TeilerConst.CROSS_ORIGINS_SV) String[] crossOrigins) {
    CorsConfiguration configuration = new CorsConfiguration();
    //configuration.setAllowedOrigins(fetchCrossOrigins(crossOrigins));
    configuration.setAllowedOrigins(Arrays.asList(crossOrigins));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT"));
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Cache-Control", "Content-Type", "Origin",
            TeilerConst.API_KEY_HEADER));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /*
  List<String> fetchCrossOrigins(String crossOrigins) {
    return (crossOrigins != null) ? Arrays.asList(
        crossOrigins.split(TeilerConst.CROSS_ORIGINS_SEPARATOR)) : new ArrayList<>();
  }

   */

}
