package org.zigmoi.ketchup.tenantsetting.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.tenantsetting.entities.ContainerRegistrySettingEntity;

import java.util.List;
import java.util.Optional;


public interface ContainerRegistrySettingRepository extends JpaRepository<ContainerRegistrySettingEntity, String> {

    Optional<ContainerRegistrySettingEntity> findByTenantIdAndId(String tenantId, String id);

    void deleteByTenantIdAndId(String tenantId, String id);

    List<ContainerRegistrySettingEntity> findAllByTenantId(String tenantId);
}
