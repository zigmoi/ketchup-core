package org.zigmoi.ketchup.iam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.iam.entities.CloudCluster;

import java.util.List;
import java.util.Optional;


public interface CloudClusterRepository extends JpaRepository<CloudCluster, String> {

    Optional<CloudCluster> findByTenantIdAndId(String tenantId, String id);

    void deleteByTenantIdAndId(String tenantId, String id);

    List<CloudCluster> findAllByTenantId(String tenantId);
}