package de.samply.security;

import de.samply.exporter.ExporterConst;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Configuration
@EnableWebSecurity
@Order(2)
public class SecurityConfiguration {
    @Value("${OIDC_GROUPS:}")
    private String allowedGroupsEnv;

    @Value(ExporterConst.SECURITY_ENABLED_SV)
    private boolean isSecurityEnabled;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String jwksUri;

    @PostConstruct
    public void init() {
        if (allowedGroupsEnv == null || allowedGroupsEnv.trim().isEmpty()) {
            throw new IllegalStateException("Allowed groups are not configured properly.");
        }
        System.out.println("Allowed groups: " + allowedGroupsEnv);
    }

    /**
     * Creates a {@link JwtDecoder} bean using the configured issuer URI.
     * @return a configured {@link  JwtDecoder} instance
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(jwksUri);
    }

    /**
     * Filter Chain for the Exporter
     * If security is enabled, the application is configured as an OAuth2 resource server that:
     * <ul>
     *     <li>Disables CSRF protection (suitable for stateless REST APIs).</li>
     *     <li>Uses JWT tokens for authentication via a custom decoder and authentication converter.</li>
     *     <li>Applies access control via a custom {@link org.springframework.security.authorization.AuthorizationManager}.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} to modify
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while configuring the security filter chain
     */
    @Bean(name = "oauthFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (!isSecurityEnabled) {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            return http.build();
        }
        http.csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .authorizeHttpRequests(authz -> authz.anyRequest().access(groupAuthorizationManager())
                );
        return http.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> groupAuthorizationManager() {
        return (authentication, context) -> {
            Authentication auth = authentication.get();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                List<String> userGroups = jwt.getClaimAsStringList("groups");
                if (userGroups == null || allowedGroupsEnv.trim().isEmpty()) {
                    return new AuthorizationDecision(false);
                }
                List<String> allowedGroups = Arrays.stream(allowedGroupsEnv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                boolean isAuthorized = userGroups.stream().anyMatch(allowedGroups::contains);
                return new AuthorizationDecision(isAuthorized);
            }
            return new AuthorizationDecision(false);
        };
    }

    /**
     *
     * @return
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }

    /**
     * @return
     */
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        BearerTokenResolver resolver = request -> {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("jwt".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            // Fallback to use the Authorization-Header
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return null;
        };
        return resolver;
    }

    private AuthenticationSuccessHandler successHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            private RequestCache requestCache = new HttpSessionRequestCache();

            @Override
            public void onAuthenticationSuccess(
                    HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                SavedRequest savedRequest = requestCache.getRequest(request, response);
                setUseReferer(true);
                if (savedRequest != null) {
                    String targetUrl = savedRequest.getRedirectUrl();
                    getRedirectStrategy().sendRedirect(request, response, targetUrl);
                } else {
                    super.onAuthenticationSuccess(request, response, authentication);
                }
            }
        };
    }

}
