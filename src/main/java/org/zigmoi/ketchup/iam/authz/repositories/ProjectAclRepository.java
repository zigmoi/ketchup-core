package org.zigmoi.ketchup.iam.authz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.iam.authz.entities.ProjectAcl;
import org.zigmoi.ketchup.project.entities.ProjectId;

import java.util.List;
import java.util.Set;

public interface ProjectAclRepository extends JpaRepository<ProjectAcl, String> {

    long deleteAllByIdentityAndPermissionId(String identity, String permissionId);

    long deleteAllByIdentityAndPermissionIdAndEffect(String identity, String permissionId, String effect);

    long deleteAllByIdentityAndPermissionIdAndProjectIdAndEffect(String identity, String permissionId, ProjectId projectId, String effect);

    @Query("SELECT distinct p.identity from ProjectAcl p where p.effect = :effect and p.projectId in :projectIds")
    Set<String> findAllMembersByEffectAndProjectIdIn(String effect, List<ProjectId> projectIds);

    @Query("SELECT distinct p.projectId from ProjectAcl p where p.identity =:identity and p.effect = :effect")
    Set<ProjectId> findAllProjectsByIdentityAndEffect(String identity, String effect);

//    @Query("delete from ProjectAcl p where p.projectId.resourceId <> '*' and  p.identity =:identity and  p.permissionId =:permissionId and p.effect = 'ALLOW'")
//    void deleteExtraAllowAcls(String identity, String permissionId);

    boolean existsByIdentityAndPermissionIdAndEffectAndProjectId(String identity, String permissionId, String effect, ProjectId projectId);

}
