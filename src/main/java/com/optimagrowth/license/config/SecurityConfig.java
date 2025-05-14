package com.optimagrowth.license.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          List<GrantedAuthority> authorities = new ArrayList<>();

          // Add roles from realm_access.roles
          List<String> realmRoles =
              jwt.getClaimAsMap("realm_access") != null
                  ? (List<String>) ((Map<String, Object>) jwt.getClaim("realm_access")).get("roles")
                  : Collections.emptyList();

          realmRoles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));

          // Add roles from resource_access.ostock.roles
          Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
          if (resourceAccess != null && resourceAccess.containsKey("ostock")) {
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("ostock");
            List<String> clientRoles = (List<String>) clientAccess.get("roles");
            clientRoles.forEach(
                role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
          }

          return authorities;
        });
    return converter;
  }
}
