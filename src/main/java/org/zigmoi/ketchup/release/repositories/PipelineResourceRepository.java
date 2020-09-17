package org.zigmoi.ketchup.release.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.release.entities.PipelineResource;
import org.zigmoi.ketchup.release.entities.PipelineResourceId;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;

import java.util.Optional;
import java.util.Set;

public interface PipelineResourceRepository extends JpaRepository<PipelineResource, PipelineResourceId> {
    void deleteAllByReleaseResourceId(String releaseRourceId);
    Set<PipelineResource> findDistinctByReleaseResourceId(String releaseResourceId);
    Optional<PipelineResource> findByReleaseResourceIdAndResourceType(String releaseResourceId, String resourceType);
}
