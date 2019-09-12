package org.zigmoi.ketchup.tenantsetting.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.tenantsetting.entities.BuildToolSettingEntity;

import java.util.List;
import java.util.Optional;


public interface BuildToolSettingRepository extends JpaRepository<BuildToolSettingEntity, String> {

    Optional<BuildToolSettingEntity> findByTenantIdAndId(String tenantId, String id);

    void deleteByTenantIdAndId(String tenantId, String id);

    List<BuildToolSettingEntity> findAllByTenantId(String tenantId);
}
