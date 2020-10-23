package org.zigmoi.ketchup.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.application.entities.PipelineArtifact;
import org.zigmoi.ketchup.application.entities.PipelineArtifactId;

import java.util.Optional;
import java.util.Set;

public interface PipelineArtifactRepository extends JpaRepository<PipelineArtifact, PipelineArtifactId> {
    @Query("delete from PipelineArtifact p where p.id.revisionResourceId = :revisionResourceId")
    void deleteAllByRevisionResourceId(String revisionResourceId);

    @Query("select distinct p from PipelineArtifact p where p.id.revisionResourceId = :revisionResourceId")
    Set<PipelineArtifact> findDistinctByRevisionResourceId(String revisionResourceId);

    @Query("select distinct p from PipelineArtifact p where p.id.revisionResourceId = :revisionResourceId and p.resourceType = :resourceType")
    Optional<PipelineArtifact> findByRevisionResourceIdAndResourceType(String revisionResourceId, String resourceType);
}
