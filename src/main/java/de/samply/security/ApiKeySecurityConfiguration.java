package de.samply.security;

import de.samply.exporter.ExporterConst;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

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
    @Bean(name = "apiKeyFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        OrRequestMatcher pathMatcher = new OrRequestMatcher(
                Arrays.stream(ExporterConst.REST_PATHS_WITH_AUTH)
                        .map(AntPathRequestMatcher::new)
                        .toArray(org.springframework.security.web.util.matcher.RequestMatcher[]::new)
        );
        AndRequestMatcher apiKeyOnPaths = new AndRequestMatcher(new ApiKeyRequestMatcher(), pathMatcher);

        httpSecurity
                .securityMatcher(apiKeyOnPaths)
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(createApiKeyFilter(), BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(apiKeyAuthEntryPoint())
                        .accessDeniedHandler(apiKeyAccessDeniedHandler())
                )
                .anonymous(anon -> anon.disable());
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
    public AuthenticationEntryPoint apiKeyAuthEntryPoint() {
        return (request, response, authException) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "ApiKey realm=\"Exporter\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String body = """
        {
          "error": "unauthorized",
          "error_description": "API key missing or invalid",
          "status": 401,
          "path": "%s",
          "timestamp": "%s"
        }
        """.formatted(request.getRequestURI(), java.time.OffsetDateTime.now().toString());

            response.getWriter().write(body);
        };
    }

    @Bean
    public AccessDeniedHandler apiKeyAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            String body = """
        {
          "error": "forbidden",
          "error_description": "You do not have permission to access this resource",
          "status": 403,
          "path": "%s",
          "timestamp": "%s"
        }
        """.formatted(request.getRequestURI(), java.time.OffsetDateTime.now().toString());

            response.getWriter().write(body);
        };
    }
}
