package org.zigmoi.ketchup.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.project.entities.ProjectAcl;
import org.zigmoi.ketchup.project.entities.ProjectId;

import java.util.List;
import java.util.Set;

public interface ProjectAclRepository extends JpaRepository<ProjectAcl, String> {

    long deleteAllByIdentityAndPermissionId(String identity, String permissionId);

    long deleteAllByIdentityAndPermissionIdAndEffect(String identity, String permissionId, String effect);

    long deleteAllByIdentityAndPermissionIdAndProjectResourceIdAndEffect(String identity, String permissionId, String projectResourceId, String effect);

    boolean existsByIdentityAndPermissionIdAndEffectAndProjectResourceId(String identity, String permissionId, String effect, String projectResourceId);

}
