package com.optimagrowth.license.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;

public class JwtInterceptor implements ClientHttpRequestInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Check if the authentication is present and if the credentials are of type Jwt
    if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
      String token = jwt.getTokenValue();
      request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
      logger.debug("JWT token added to request header");
    } else {
      logger.warn("JWT token not available in SecurityContext");
    }

    return execution.execute(request, body);
  }
}
