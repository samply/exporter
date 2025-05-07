package de.samply.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class ProjectUserJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<SimpleGrantedAuthority>> {

    @Value("${OIDC_GROUPS:}")
    private String allowedGroupsEnv;

    /**
     *
     * @param jwt
     * @return
     */
    public List<SimpleGrantedAuthority> convert(Jwt jwt) {
        List<String> allowedGroups = Arrays.stream(allowedGroupsEnv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        List<String> userGroups = jwt.getClaimAsStringList("groups");
        return userGroups.stream()
                .filter(allowedGroups::contains)
                .map(group -> new SimpleGrantedAuthority("ROLE_" + group))
                .toList();
    }
}
