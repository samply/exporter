package de.samply.security;

import de.samply.exporter.ExporterConst;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Order(0)
public class NoAuthSecurityConfiguration {
    @Bean
    SecurityFilterChain publicChain(HttpSecurity httpSecurity) throws Exception {
        var publicPaths = new OrRequestMatcher(
                Arrays.stream(ExporterConst.REST_PATHS_NO_AUTH)
                        .map(AntPathRequestMatcher::new)
                        .toArray(RequestMatcher[]::new)
        );

        return httpSecurity.securityMatcher(publicPaths)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}