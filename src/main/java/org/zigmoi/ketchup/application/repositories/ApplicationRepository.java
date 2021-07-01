package org.zigmoi.ketchup.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.application.entities.Application;
import org.zigmoi.ketchup.application.entities.ApplicationId;

import javax.persistence.Tuple;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, ApplicationId> {
    @Query("select a from Application a where a.id.applicationResourceId = :applicationResourceId")
    Application getByApplicationResourceId(String applicationResourceId);

    @Query("select count(a) from Application a where a.id.projectResourceId = :projectResourceId")
    long getAllApplicationsByProjectResourceId(String projectResourceId);

    @Query(value = "select a.id, a.type, a.displayName, a.createdOn, a.createdBy, a.lastUpdatedOn, a.lastUpdatedBy  from Application a where a.id.projectResourceId = :projectResourceId")
    List<Tuple> listAllApplicationsInProjectCustomByProjectResourceId(String projectResourceId);
}
