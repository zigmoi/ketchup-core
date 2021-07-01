package org.zigmoi.ketchup.application.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.application.entities.Revision;
import org.zigmoi.ketchup.application.entities.RevisionId;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RevisionRepository extends JpaRepository<Revision, RevisionId> {
    @Query("select r from Revision r where r.id.revisionResourceId = :revisionResourceId")
    Optional<Revision> findByRevisionResourceId(String revisionResourceId);

    @Query("select distinct r from Revision r where r.id.applicationResourceId = :applicationResourceId order by r.createdOn DESC")
    Set<Revision> findDistinctByApplicationResourceIdOrderByCreatedOnDesc(String applicationResourceId);

    @Query("select r.id, r.createdOn, r.createdBy, r.lastUpdatedOn, r.lastUpdatedBy, r.version, r.status, r.deploymentTriggerType, r.commitId," +
            " r.helmChartId, r.helmReleaseId, r.helmReleaseVersion, r.rollback, r.originalRevisionVersionId" +
            " from Revision r where r.id.applicationResourceId = :applicationResourceId order by r.createdOn DESC")
    List<Tuple> findApplicationResourceRevisionsByApplicationResourceId(String applicationResourceId);

    @Query("select r from Revision r where r.id.applicationResourceId = :applicationResourceId")
    List<Revision> findAllByApplicationResourceId(String applicationResourceId);

    @Query("select distinct r from Revision r where r.id.projectResourceId = :projectResourceId")
    Set<Revision> findDistinctByProjectResourceId(String projectResourceId);

    @Query("select distinct r from Revision r where r.id.projectResourceId = :projectResourceId and r.rollback = false order by r.lastUpdatedOn DESC")
    List<Revision> listRecentRevisionPipelinesInProject(String projectResourceId, Pageable pageable);

    @Query("select distinct r from Revision r where r.id.projectResourceId = :projectResourceId AND r.rollback = false and r.status = :status order by r.lastUpdatedOn DESC")
    Set<Revision> listAllRevisionPipelinesInProjectWithStatus(String projectResourceId, String status);

    @Query("select count(ALL r) from Revision r where r.id.applicationResourceId = :applicationResourceId")
    long countAllByApplicationResourceId(String applicationResourceId);

    @Query("select distinct r from Revision r where r.id.applicationResourceId = :applicationResourceId order by r.createdOn DESC")
    List<Revision> findCurrentRevision(String applicationResourceId, Pageable pageable);

    @Query("select distinct r from Revision r where r.id.applicationResourceId = :applicationResourceId and r.status = :status order by r.lastUpdatedOn DESC")
    List<Revision> findLastSuccessfulRevision(String applicationResourceId, String status, Pageable pageable);

    @Modifying
    @Query("delete from Revision r where r.id.applicationResourceId = :applicationResourceId")
    void deleteAllByApplicationResourceId(String applicationResourceId);

    @Query("select count(ALL r) from Revision r where r.id.projectResourceId = :projectResourceId")
    long countAllRevisionsInProject(String projectResourceId);

    @Query("select count(ALL r) from Revision r where r.id.projectResourceId = :projectResourceId and r.rollback = false")
    long countAllRevisionPipelinesInProject(String projectResourceId);
}
