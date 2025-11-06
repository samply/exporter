package de.samply.security;

import de.samply.exporter.ExporterConst;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Configuration
@EnableWebSecurity
@Order(2)
public class OAuthSecurityConfiguration {
    private static final Logger log = LoggerFactory.getLogger(OAuthSecurityConfiguration.class);
    @Value("${OIDC_GROUPS:}")
    private String allowedGroupsEnv;

    @Value(ExporterConst.SECURITY_ENABLED_SV)
    private boolean isSecurityEnabled;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @PostConstruct
    public void init() {
        if (allowedGroupsEnv == null || allowedGroupsEnv.trim().isEmpty()) {
            throw new IllegalStateException("Allowed groups are not configured properly.");
        }
        log.info("Allowed groups: " + allowedGroupsEnv);
    }

    /**
     * Creates a {@link JwtDecoder} bean using the configured issuer URI.
     * @return a configured {@link  JwtDecoder} instance
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        JwtDecoder delegate = JwtDecoders.fromIssuerLocation(issuerUri);

        return token -> {
            try {
                Jwt jwt = delegate.decode(token);
                log.debug("[JWT OK] sub=" + jwt.getSubject()
                        + " aud=" + jwt.getAudience()
                        + " iss=" + jwt.getIssuer()
                        + " exp=" + jwt.getExpiresAt());
                return jwt;
            } catch (JwtException e) {
                log.warn("[JWT FAIL] {}", e.getMessage());
                throw e;
            }
            catch (Exception e) {
                throw new JwtException( "[JWT FAIL] jwt is not valid.");
            }
        };
    }

    /**
     * Filter Chain for the Exporter
     * If security is enabled, the application is configured as an OAuth2 resource server that:
     * <ul>
     *     <li>Disables CSRF protection (suitable for stateless REST APIs).</li>
     *     <li>Uses JWT tokens for authentication via a custom decoder and authentication converter.</li>
     *     <li>Applies access control via a custom {@link AuthorizationManager}.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} to modify
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while configuring the security filter chain
     */
    @Bean(name = "oauthFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .requestCache(rc -> rc.disable())
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(jwtAuthEntryPoint())
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(ExporterConst.REST_PATHS_NO_AUTH).permitAll()
                        .anyRequest()
                        .access(groupAuthorizationManager())
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(jwtAuthEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );
        httpSecurity.addFilterBefore((req, res, chain) -> {
            HttpServletRequest r = (HttpServletRequest) req;
            String h = r.getHeader(HttpHeaders.AUTHORIZATION);
            log.info("[CHECK HEADER in JWT Chain before BearerTokenAuthenticationFilter] " + h);
            chain.doFilter(req, res);
        }, BearerTokenAuthenticationFilter.class);
        return httpSecurity.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> groupAuthorizationManager() {
        List<String> allowedGroups = Arrays.stream(allowedGroupsEnv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return (authentication, context) -> {
            String test = authentication.get().getDetails().toString();
            Authentication auth = authentication.get();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                List<String> userGroups = jwt.getClaimAsStringList("groups");
                log.debug("JWT groups     : {}", userGroups);
                log.debug("Allowed groups : {}", allowedGroups);
                boolean isAuthorized = userGroups != null && userGroups.stream().anyMatch(allowedGroups::contains);
                log.debug("JWT has allowed groups: {}", isAuthorized);
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
    public JwtAuthenticationConverter jwtAuthenticationConverter() { return new JwtAuthenticationConverter();}

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

    @Bean
    public AuthenticationEntryPoint jwtAuthEntryPoint() {
        return (request, response, authException) -> {
            Throwable cause = authException.getCause();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            if (cause instanceof JwtException jwtEx && jwtEx.getMessage().contains("expired")) {
                response.getWriter().write("""
                {"error":"token_expired","message":"Your access token has expired. Please refresh or log in again."}
            """);
            } else {
                response.getWriter().write("""
                {"error":"unauthorized","message":"Invalid or missing access token."}
            """);
            }
        };
    }
}