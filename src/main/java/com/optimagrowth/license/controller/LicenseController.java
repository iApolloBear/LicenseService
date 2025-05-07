package com.optimagrowth.license.controller;

import com.optimagrowth.license.model.License;
import com.optimagrowth.license.service.LicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "v1/organization/{organizationId}/license")
public class LicenseController {
  private final LicenseService licenseService;

  public LicenseController(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  @GetMapping(value = "/{licenseId}")
  public ResponseEntity<License> getLicense(
      @PathVariable("organizationId") String organizationId,
      @PathVariable("licenseId") String licenseId) {
    License license = licenseService.getLicense(licenseId, organizationId, "");
    license.add(
        linkTo(methodOn(LicenseController.class).getLicense(organizationId, license.getLicenseId()))
            .withSelfRel());
    license.add(
        linkTo(methodOn(LicenseController.class).createLicense(license)).withRel("createLicense"));
    license.add(
        linkTo(methodOn(LicenseController.class).updateLicense(license)).withRel("updateLicense"));
    license.add(
        linkTo(methodOn(LicenseController.class).deleteLicense(license.getLicenseId()))
            .withRel("deleteLicense"));
    return ResponseEntity.ok(license);
  }

  @GetMapping("/{licenseId}/{clientType}")
  public License getLicensesWithClient(
      @PathVariable String organizationId,
      @PathVariable String licenseId,
      @PathVariable String clientType) {
    return this.licenseService.getLicense(organizationId, licenseId, clientType);
  }

  @PutMapping
  public ResponseEntity<License> updateLicense(@RequestBody License request) {
    return ResponseEntity.ok(licenseService.updateLicense(request));
  }

  @PostMapping
  public ResponseEntity<License> createLicense(@RequestBody License request) {
    return ResponseEntity.ok(licenseService.createLicense(request));
  }

  @DeleteMapping(value = "/{licenseId}")
  public ResponseEntity<String> deleteLicense(@PathVariable("licenseId") String licenseId) {
    return ResponseEntity.ok(licenseService.deleteLicense(licenseId));
  }

  @GetMapping
  public List<License> getLicenses(@PathVariable String organizationId) throws TimeoutException {
    return this.licenseService.getLicensesByOrganization(organizationId);
  }
}
