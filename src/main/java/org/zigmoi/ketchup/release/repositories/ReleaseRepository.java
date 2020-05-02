package org.zigmoi.ketchup.release.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;

public interface ReleaseRepository extends JpaRepository<Release, ReleaseId> {
}
