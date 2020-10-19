package org.zigmoi.ketchup.release.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;

import javax.persistence.OrderBy;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReleaseRepository extends JpaRepository<Release, ReleaseId> {
    Set<Release> findDistinctByDeploymentResourceIdOrderByCreatedOnDesc(String deploymentResourceId);
    List<Release> findAllByDeploymentResourceId(String deploymentResourceId);
    Set<Release> findDistinctByProjectResourceId(String projectResourceId);
    Set<Release> findDistinctTop5ByProjectResourceIdOrderByLastUpdatedOnDesc(String projectResourceId);
    Set<Release> findDistinctByProjectResourceIdAndStatusOrderByLastUpdatedOnDesc(String projectResourceId, String status);
    long countAllByDeploymentResourceId(String deploymentResourceId);
    Optional<Release> findTopByDeploymentResourceIdAndStatusOrderByLastUpdatedOnDesc(String deploymentResourceId, String status);
    void deleteAllByDeploymentResourceId(String deploymentResourceId);
}
