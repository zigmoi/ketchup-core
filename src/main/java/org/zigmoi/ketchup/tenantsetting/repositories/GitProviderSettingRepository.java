package org.zigmoi.ketchup.tenantsetting.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.tenantsetting.entities.GitProviderSettingEntity;

import java.util.List;
import java.util.Optional;


public interface GitProviderSettingRepository extends JpaRepository<GitProviderSettingEntity, String> {

    Optional<GitProviderSettingEntity> findByTenantIdAndId(String tenantId, String id);

    void deleteByTenantIdAndId(String tenantId, String id);

    List<GitProviderSettingEntity> findAllByTenantId(String tenantId);
}
