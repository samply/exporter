package de.samply.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
public class GroupAuthManager {
    private static final Logger log = LoggerFactory.getLogger(GroupAuthManager.class);
    @Value("${OIDC_GROUPS:}")
    private String allowedGroupsEnv;

    @PostConstruct
    public void init() {
        if (allowedGroupsEnv == null || allowedGroupsEnv.trim().isEmpty()) {
            throw new IllegalStateException("Allowed groups are not configured properly.");
        }
        log.info("Allowed groups: " + allowedGroupsEnv);
    }

    @Bean
    public AuthorizationManager<RequestAuthorizationContext> groupAuthorizationManager() {
        List<String> allowedGroups = Arrays.stream(allowedGroupsEnv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        return (authentication, context) -> {
            Authentication auth = authentication.get();
            List<String> userGroups = null;

            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                userGroups = jwt.getClaimAsStringList("groups");
            }

            else if (auth instanceof OAuth2AuthenticationToken oauth2Auth) {
                Object principal = oauth2Auth.getPrincipal();

                if (principal instanceof OidcUser oidcUser) {
                    userGroups = oidcUser.getClaimAsStringList("groups");
                } else if (principal instanceof OAuth2User oauth2User) {
                    Object claim = oauth2User.getAttributes().get("groups");
                    if (claim instanceof List<?> list) {
                        userGroups = list.stream()
                                .filter(String.class::isInstance)
                                .map(String.class::cast)
                                .toList();
                    }
                }
            }

            boolean isAuthorized = userGroups != null && userGroups.stream().anyMatch(allowedGroups::contains);

            log.debug("Auth type        : {}", auth.getClass().getSimpleName());
            log.debug("User groups      : {}", userGroups);
            log.debug("Allowed groups   : {}", allowedGroups);
            log.debug("Has allowed group: {}", isAuthorized);

            return new AuthorizationDecision(isAuthorized);
        };
    }
}
