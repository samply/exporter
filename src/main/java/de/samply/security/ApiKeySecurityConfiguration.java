package de.samply.security;

import de.samply.exporter.ExporterConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import java.util.Arrays;


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
                .securityMatcher(ExporterConst.REST_PATHS_WITH_API_KEY)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilter(createApiKeyFilter())
                .authorizeHttpRequests(authorize -> {
                            authorize.requestMatchers(new AntPathRequestMatcher(ExporterConst.API_DOCS)).permitAll();
                            Arrays.stream(ExporterConst.REST_PATHS_WITH_API_KEY).forEach(path -> authorize.requestMatchers(new AntPathRequestMatcher(path)).authenticated());
                            authorize.anyRequest().authenticated();
                        }
                );
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
