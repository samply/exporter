package de.samply.security;


import de.samply.teiler.TeilerConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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

}
