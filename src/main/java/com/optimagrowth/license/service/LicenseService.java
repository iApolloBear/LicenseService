package com.optimagrowth.license.service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.service.client.OrganizationDiscoveryClient;
import com.optimagrowth.license.service.client.OrganizationFeignClient;
import com.optimagrowth.license.service.client.OrganizationRestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
public class LicenseService {
  private final MessageSource messages;
  private final LicenseRepository licenseRepository;
  private final ServiceConfig config;
  private final OrganizationFeignClient organizationFeignClient;
  private final OrganizationRestTemplate organizationRestClient;
  private final OrganizationDiscoveryClient organizationDiscoveryClient;

  private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

  public LicenseService(
      MessageSource messages,
      LicenseRepository licenseRepository,
      ServiceConfig config,
      OrganizationFeignClient organizationFeignClient,
      OrganizationRestTemplate organizationRestClient,
      OrganizationDiscoveryClient organizationDiscoveryClient) {
    this.messages = messages;
    this.licenseRepository = licenseRepository;
    this.config = config;
    this.organizationFeignClient = organizationFeignClient;
    this.organizationRestClient = organizationRestClient;
    this.organizationDiscoveryClient = organizationDiscoveryClient;
  }

  public License getLicense(String licenseId, String organizationId, String clientType) {
    License license =
        this.licenseRepository
            .findByOrganizationIdAndLicenseId(licenseId, organizationId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        String.format(
                            messages.getMessage("license.search.error.message", null, null),
                            licenseId,
                            organizationId)));
    Organization organization = this.retrieveOrganizationInfo(organizationId, clientType);
    if (organization != null) {
      license.setOrganizationName(organization.getName());
      license.setContactName(organization.getContactName());
      license.setContactEmail(organization.getContactEmail());
      license.setContactPhone(organization.getContactPhone());
    }

    return license.withComment(this.config.getProperty());
  }

  private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
    Organization organization = null;

    switch (clientType) {
      case "feign":
        System.out.println("I am using the feign client");
        organization = this.organizationFeignClient.getOrganization(organizationId);
        break;
      case "rest":
        System.out.println("I am using the rest client");
        organization = this.organizationRestClient.getOrganization(organizationId);
        break;
      case "discovery":
        System.out.println("I am using the discovery client");
        organization = this.organizationDiscoveryClient.getOrganization(organizationId);
        break;
      default:
        organization = this.organizationRestClient.getOrganization(organizationId);
        break;
    }

    return organization;
  }

  public License createLicense(License license) {
    license.setLicenseId(UUID.randomUUID().toString());
    this.licenseRepository.save(license);
    return license.withComment(this.config.getProperty());
  }

  public License updateLicense(License license) {
    this.licenseRepository.save(license);
    return license.withComment(this.config.getProperty());
  }

  public String deleteLicense(String licenseId) {
    String responseMessage = null;
    License license = new License();
    license.setLicenseId(licenseId);
    this.licenseRepository.delete(license);
    responseMessage =
        String.format(messages.getMessage("license.delete.message", null, null), licenseId);
    return responseMessage;
  }

  private void randomlyRunLong() throws TimeoutException {
    Random rand = new Random();
    int randomNum = rand.nextInt(3) + 1;
    if (randomNum == 3) sleep();
  }

  private void sleep() throws TimeoutException {
    try {
      Thread.sleep(5000);
      throw new java.util.concurrent.TimeoutException();
    } catch (InterruptedException e) {
      logger.error(e.getMessage());
    }
  }

  @CircuitBreaker(name = "licenseService")
  public List<License> getLicensesByOrganization(String organizationId) throws TimeoutException {
    this.randomlyRunLong();
    return this.licenseRepository.findByOrganizationId(organizationId);
  }
}
