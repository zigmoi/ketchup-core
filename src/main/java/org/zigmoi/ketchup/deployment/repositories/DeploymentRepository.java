package org.zigmoi.ketchup.deployment.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;

public interface DeploymentRepository extends JpaRepository<DeploymentEntity, DeploymentId> {
    @Query("select d from DeploymentEntity d where d.id.deploymentResourceId = :deploymentResourceId")
    DeploymentEntity getByDeploymentResourceId(String deploymentResourceId);
}
