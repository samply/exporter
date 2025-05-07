package de.samply.security;

import de.samply.exporter.ExporterConst;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CommonSecurityBeans {

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

}
