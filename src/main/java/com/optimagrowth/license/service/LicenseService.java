package com.optimagrowth.license.service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.repository.LicenseRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class LicenseService {
  private final MessageSource messages;
  private final LicenseRepository licenseRepository;
  private final ServiceConfig config;

  public LicenseService(
      MessageSource messages, LicenseRepository licenseRepository, ServiceConfig serviceConfig) {
    this.messages = messages;
    this.licenseRepository = licenseRepository;
    this.config = serviceConfig;
  }

  public License getLicense(String licenseId, String organizationId) {
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
    return license.withComment(this.config.getProperty());
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
}
