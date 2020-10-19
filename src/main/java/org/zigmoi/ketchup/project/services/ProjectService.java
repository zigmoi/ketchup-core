package org.zigmoi.ketchup.project.services;

import org.zigmoi.ketchup.project.dtos.ProjectDto;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectService {

    void createProject(ProjectDto projectDto);

    void deleteProject(String projectResourceId);

    void updateDescription(String projectResourceId, String description);

    List<Project> listAllProjects();

    Optional<Project> findById(String projectResourceId);

    boolean validateProject(ProjectId projectId);


}
