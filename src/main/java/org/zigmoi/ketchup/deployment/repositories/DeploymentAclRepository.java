package org.zigmoi.ketchup.deployment.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zigmoi.ketchup.deployment.entities.DeploymentAcl;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;

import java.util.List;
import java.util.Set;

public interface DeploymentAclRepository extends JpaRepository<DeploymentAcl, String> {

    long deleteAllByIdentityAndPermissionId(String identity, String permissionId);

    long deleteAllByIdentityAndPermissionIdAndEffect(String identity, String permissionId, String effect);

    long deleteAllByIdentityAndPermissionIdAndDeploymentIdAndEffect(String identity, String permissionId, DeploymentId DeploymentId, String effect);

    @Query("SELECT distinct p.identity from DeploymentAcl p where p.effect = :effect and p.DeploymentId in :DeploymentIds")
    Set<String> findAllMembersByEffectAndDeploymentIdIn(String effect, List<DeploymentId> DeploymentIds);

    @Query("SELECT distinct p.DeploymentId from DeploymentAcl p where p.identity =:identity and p.effect = :effect")
    Set<DeploymentId> findAllDeploymentsByIdentityAndEffect(String identity, String effect);

//    @Query("delete from DeploymentAcl p where p.DeploymentId.resourceId <> '*' and  p.identity =:identity and  p.permissionId =:permissionId and p.effect = 'ALLOW'")
//    void deleteExtraAllowAcls(String identity, String permissionId);

    boolean existsByIdentityAndPermissionIdAndEffectAndDeploymentId(String identity, String permissionId, String effect, DeploymentId DeploymentId);

}
