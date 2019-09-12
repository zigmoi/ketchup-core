package org.zigmoi.ketchup.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;

import java.util.Set;

public interface ProjectRepository extends JpaRepository<Project, ProjectId> {
//    boolean existsByIdAndMembersExists(ProjectId projectId, String member);

    @Query("select distinct p.id from Project p")
    Set<ProjectId> findAllProjectIds();
}
