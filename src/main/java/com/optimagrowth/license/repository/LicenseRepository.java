package com.optimagrowth.license.repository;

import com.optimagrowth.license.model.License;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends CrudRepository<License, String> {
  List<License> findByOrganizationId(String organizationId);

  Optional<License> findByOrganizationIdAndLicenseId(String organizationId, String licenseId);
}
