package org.zigmoi.ketchup.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.application.entities.Application;
import org.zigmoi.ketchup.application.entities.ApplicationId;

public interface ApplicationRepository extends JpaRepository<Application, ApplicationId> {
    @Query("select d from Application d where d.id.applicationResourceId = :applicationResourceId")
    Application getByApplicationResourceId(String applicationResourceId);
}
