package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganizationRestTemplate {
  private final RestTemplate restTemplate;

  public OrganizationRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public Organization getOrganization(String organizationId) {
    ResponseEntity<Organization> restExchange =
        this.restTemplate.exchange(
            "http://organization-service/v1/organization/{organizationId}",
            HttpMethod.GET,
            null,
            Organization.class,
            organizationId);
    return restExchange.getBody();
  }
}
