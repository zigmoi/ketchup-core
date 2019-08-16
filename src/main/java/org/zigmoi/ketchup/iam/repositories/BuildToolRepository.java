package org.zigmoi.ketchup.iam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.iam.entities.BuildTool;

import java.util.List;
import java.util.Optional;


public interface BuildToolRepository extends JpaRepository<BuildTool, String> {

    Optional<BuildTool> findByTenantIdAndId(String tenantId, String id);

    void deleteByTenantIdAndId(String tenantId, String id);

    List<BuildTool> findAllByTenantId(String tenantId);
}