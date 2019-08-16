package org.zigmoi.ketchup.iam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.iam.entities.CloudCredential;

import java.util.List;
import java.util.Optional;


public interface CloudCredentialRepository extends JpaRepository<CloudCredential, String> {

    Optional<CloudCredential> findByTenantIdAndId(String tenantId, String id);

    void deleteByTenantIdAndId(String tenantId, String id);

    List<CloudCredential> findAllByTenantId(String tenantId);
}