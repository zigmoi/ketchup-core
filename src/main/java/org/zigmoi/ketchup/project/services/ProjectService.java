package org.zigmoi.ketchup.project.services;

import org.springframework.validation.annotation.Validated;
import org.zigmoi.ketchup.project.dtos.ProjectDto;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Validated
public interface ProjectService {

    void createProject(@Valid ProjectDto projectDto);

    void deleteProject(@ValidProjectId String projectResourceId);

    void updateProject(@Valid ProjectDto projectDto);

    List<Project> listAllProjects();

    Optional<Project> findById(@ValidProjectId String projectResourceId);

    boolean validateProject(@Valid ProjectId projectId);

}
