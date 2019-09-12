package org.zigmoi.ketchup.tenantsetting.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.tenantsetting.entities.CloudProviderSettingEntity;

import java.util.List;
import java.util.Optional;


public interface CloudProviderSettingRepository extends JpaRepository<CloudProviderSettingEntity, String> {

    Optional<CloudProviderSettingEntity> findByTenantIdAndId(String tenantId, String id);

    void deleteByTenantIdAndId(String tenantId, String id);

    List<CloudProviderSettingEntity> findAllByTenantId(String tenantId);
}
