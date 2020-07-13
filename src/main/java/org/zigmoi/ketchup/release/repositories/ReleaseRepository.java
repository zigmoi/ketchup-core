package org.zigmoi.ketchup.release.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;

import javax.persistence.OrderBy;
import java.util.List;
import java.util.Set;

public interface ReleaseRepository extends JpaRepository<Release, ReleaseId> {
    Set<Release> findDistinctByDeploymentResourceIdOrderByCreatedOnDesc(String deploymentResourceId);
    Set<Release> findDistinctByProjectResourceId(String projectResourceId);
    long countAllByDeploymentResourceId(String deploymentResourceId);
}