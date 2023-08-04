package de.samply.security;

import de.samply.exporter.ExporterConst;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * Provides authentication manager based on an api key for REST calls.
 */
@Component
public class ApiKeyAuthenticationManager implements AuthenticationManager {

  /*
   * Security: This class provides API key support to REST for connecting different server
   */

  @Value(ExporterConst.EXPORTER_API_KEY_SV)
  private String apiKey;

  /**
   * Authenticates request based on an API key.
   *
   * @param authentication REST API Client authentication.
   * @return Authentication with setAuthenticated set to true or false.
   * @throws AuthenticationException Authentication exception.
   */
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    String apiKey = (String) authentication.getPrincipal();

    if (!ObjectUtils.isEmpty(this.apiKey) && (ObjectUtils.isEmpty(apiKey) || !apiKey.equals(
        this.apiKey))) {
      throw new BadCredentialsException("Incorrect API Key");
    } else {
      authentication.setAuthenticated(true);
    }

    return authentication;

  }

}
