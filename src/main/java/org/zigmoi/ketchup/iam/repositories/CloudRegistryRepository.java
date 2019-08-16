package org.zigmoi.ketchup.iam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.iam.entities.CloudRegistry;

import java.util.List;
import java.util.Optional;


public interface CloudRegistryRepository extends JpaRepository<CloudRegistry, String> {

    Optional<CloudRegistry> findByTenantIdAndId(String tenantId, String id);

    void deleteByTenantIdAndId(String tenantId, String id);

    List<CloudRegistry> findAllByTenantId(String tenantId);
}