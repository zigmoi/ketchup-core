package org.zigmoi.ketchup.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.application.entities.Application;
import org.zigmoi.ketchup.application.entities.ApplicationId;

public interface ApplicationRepository extends JpaRepository<Application, ApplicationId> {
    @Query("select a from Application a where a.id.applicationResourceId = :applicationResourceId")
    Application getByApplicationResourceId(String applicationResourceId);

    @Query("select count(a) from Application a where a.id.projectResourceId = :projectResourceId")
    long getAllApplicationsByProjectResourceId(String projectResourceId);
}
