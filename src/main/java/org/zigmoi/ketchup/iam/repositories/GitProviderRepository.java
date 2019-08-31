package org.zigmoi.ketchup.iam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.iam.entities.GitProvider;

import java.util.List;
import java.util.Optional;


public interface GitProviderRepository extends JpaRepository<GitProvider, String> {

    Optional<GitProvider> findByTenantIdAndId(String tenantId, String id);

    void deleteByTenantIdAndId(String tenantId, String id);

    List<GitProvider> findAllByTenantId(String tenantId);
}