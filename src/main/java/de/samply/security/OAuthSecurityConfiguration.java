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
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


import java.io.IOException;
import java.util.Arrays;

/**
 *
 */
@Configuration
@EnableWebSecurity
@Order(2)
public class OAuthSecurityConfiguration {
    private static final Logger log = LoggerFactory.getLogger(OAuthSecurityConfiguration.class);
    @Value(ExporterConst.SECURITY_ENABLED_SV)
    private boolean isSecurityEnabled;


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
    @Bean(name = ExporterConst.OAUTH_FILTER_CHAIN)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            AuthenticationEntryPoint jwtAuthEntryPoint,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            AuthorizationManager<RequestAuthorizationContext> groupAuthorizationManager
    ) throws Exception {

        RequestMatcher browserPaths = new OrRequestMatcher(
                Arrays.stream(ExporterConst.REST_PATHS_BROWSER_AUTH)
                        .map(AntPathRequestMatcher::new)
                        .toArray(RequestMatcher[]::new)
        );

        AuthorizationManager<RequestAuthorizationContext> authThenGroups =
                AuthorizationManagers.allOf(
                        AuthenticatedAuthorizationManager.authenticated(),
                        groupAuthorizationManager
                );

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .requestCache(cache -> cache.requestCache(new HttpSessionRequestCache()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage(ExporterConst.OIDC_LOGIN_PAGE)
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(ExporterConst.OAUTH2_PATHS, ExporterConst.LOGIN_PATHS).permitAll()
                        .requestMatchers(ExporterConst.REST_PATHS_NO_AUTH).permitAll()
                        .anyRequest().access(authThenGroups)   // <- use composed manager
                )
                .exceptionHandling(eh -> eh
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint(ExporterConst.OIDC_LOGIN_PAGE),
                                browserPaths
                        )
                        .defaultAuthenticationEntryPointFor(
                                jwtAuthEntryPoint,
                                AnyRequestMatcher.INSTANCE
                        )
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );

        return httpSecurity.build();
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