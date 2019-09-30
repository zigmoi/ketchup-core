package org.zigmoi.ketchup.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zigmoi.ketchup.project.entities.ProjectSettingsId;
import org.zigmoi.ketchup.project.entities.ProjectSettingsEntity;

import java.util.List;

public interface ProjectSettingsRepository extends JpaRepository<ProjectSettingsEntity, ProjectSettingsId> {
    List<ProjectSettingsEntity> findAllByIdProjectIdAndTypeAndDisplayNameLike(String projectId, String type, String displayName);
    List<ProjectSettingsEntity> findAllByIdProjectIdAndType(String projectId, String type);
}
