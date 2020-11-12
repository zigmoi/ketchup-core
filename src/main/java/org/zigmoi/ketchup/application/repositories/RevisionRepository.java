package org.zigmoi.ketchup.application.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.application.entities.Revision;
import org.zigmoi.ketchup.application.entities.RevisionId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RevisionRepository extends JpaRepository<Revision, RevisionId> {
    @Query("select r from Revision r where r.id.revisionResourceId = :revisionResourceId")
    Optional<Revision> findByRevisionResourceId(String revisionResourceId);

    @Query("select distinct r from Revision r where r.id.applicationResourceId = :applicationResourceId order by r.createdOn DESC")
    Set<Revision> findDistinctByApplicationResourceIdOrderByCreatedOnDesc(String applicationResourceId);

    @Query("select r from Revision r where r.id.applicationResourceId = :applicationResourceId")
    List<Revision> findAllByApplicationResourceId(String applicationResourceId);

    @Query("select distinct r from Revision r where r.id.projectResourceId = :projectResourceId")
    Set<Revision> findDistinctByProjectResourceId(String projectResourceId);

    @Query("select distinct r from Revision r where r.id.projectResourceId = :projectResourceId order by r.lastUpdatedOn DESC")
    List<Revision> findTop5ByProjectResourceIdOrderByLastUpdatedOnDesc(String projectResourceId, Pageable pageable);

    @Query("select distinct r from Revision r where r.id.projectResourceId = :projectResourceId AND r.status = :status order by r.lastUpdatedOn DESC")
    Set<Revision> findDistinctByProjectResourceIdAndStatusOrderByLastUpdatedOnDesc(String projectResourceId, String status);

    @Query("select count(ALL r) from Revision r where r.id.applicationResourceId = :applicationResourceId")
    long countAllByApplicationResourceId(String applicationResourceId);

    @Query("select distinct r from Revision r where r.id.applicationResourceId = :applicationResourceId AND r.status = :status order by r.lastUpdatedOn DESC")
    List<Revision> findTopByApplicationResourceIdAndStatusOrderByLastUpdatedOnDesc(String applicationResourceId, String status, Pageable pageable);

    @Modifying
    @Query("delete from Revision r where r.id.applicationResourceId = :applicationResourceId")
    void deleteAllByApplicationResourceId(String applicationResourceId);

    @Query("select count(ALL r) from Revision r where r.id.projectResourceId = :projectResourceId")
    long countAllRevisionsInProject(String projectResourceId);
}
