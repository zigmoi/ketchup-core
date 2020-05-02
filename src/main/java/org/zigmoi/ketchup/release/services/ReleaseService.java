package org.zigmoi.ketchup.release.services;

import org.zigmoi.ketchup.project.dtos.ProjectDto;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.release.entities.Release;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReleaseService {

    void create();
    void stop(String releaseResourceId);
    Optional<Release> findById(String releaseResourceId);
    void delete(String releaseResourceId);
    void update(Release release);
    Set<Release> listAllInDeployment();
    Set<Release> listAllInProject();
}
