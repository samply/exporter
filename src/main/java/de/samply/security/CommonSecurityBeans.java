package de.samply.security;

import de.samply.exporter.ExporterConst;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;


@Configuration
public class CommonSecurityBeans {
    private static final Logger log = LoggerFactory.getLogger(CommonSecurityBeans.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value(ExporterConst.CROSS_ORIGINS_SV) String[] crossOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(crossOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS"));
        configuration.setAllowedHeaders(
                Arrays.asList("Authorization", "Cache-Control", "Content-Type", "Origin"));
        configuration.setAllowCredentials(true); // Optional: allow credentials
        configuration.setExposedHeaders(Arrays.asList("Content-Disposition")); // Optional

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
     *
     * @return
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() { return new JwtAuthenticationConverter();}
}
