package org.zigmoi.ketchup.iam.authz.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zigmoi.ketchup.iam.authz.dtos.ProjectAclDto;
import org.zigmoi.ketchup.iam.authz.entities.ProjectAcl;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.iam.authz.repositories.ProjectAclRepository;
import org.zigmoi.ketchup.iam.common.AuthUtils;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.project.services.ProjectService;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class ProjectAclServiceImpl implements ProjectAclService {

    @Autowired
    private ProjectAclRepository projectAclRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Override
    @Transactional
    public void assignPermission(ProjectAclDto projectAclDto) {
        //validate each permission is valid project permission.
        //validate identity is valid user and not *.
        //if resource is not * validate its a valid resource.
        //validate if permission is create-project or assign-create-project resource is *.

        String resourceId = projectAclDto.getResourceId();
        String identity = projectAclDto.getIdentity();
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(resourceId);

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId(AuthUtils.getCurrentTenantId());
        projectIdAll.setResourceId("*");

        Set<ProjectAcl> aclsToAdd = new HashSet<>();

        Iterator<String> permissions = projectAclDto.getPermissions().iterator();
        while (permissions.hasNext()) {
            String permission = permissions.next();
            long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndEffect(identity, permission, "DENY");
            System.out.println("deletedAclsCount: " + deletedAclsCount);
            if (resourceId.equals("*")) {
                if (hasProjectPermission(identity, permission, projectIdAll) == false) {
                    ProjectAcl projectAclAllow = buildProjectAcl(identity, permission, projectId, "ALLOW");
                    aclsToAdd.add(projectAclAllow);
                    //delete extra allow entries , * entry is already not present so remove whatever is present.
                    long deletedExtraAllowAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndEffect(identity, permission, "ALLOW");
                    System.out.println("deletedExtraAllowAclsCount: " + deletedExtraAllowAclsCount);
                }
            } else {
                if (hasProjectPermission(identity, permission, projectId) == false && hasProjectPermission(identity, permission, projectIdAll) == false) {
                    ProjectAcl projectAclAllow = buildProjectAcl(identity, permission, projectId, "ALLOW");
                    aclsToAdd.add(projectAclAllow);
                }
            }
        }
        projectAclRepository.saveAll(aclsToAdd);
    }

    private ProjectAcl buildProjectAcl(String identity, String permission, ProjectId projectId, String effect) {
        String aclId = UUID.randomUUID().toString();
        ProjectAcl projectAcl = new ProjectAcl();
        projectAcl.setAclRuleId(aclId);
        projectAcl.setIdentity(identity);
        projectAcl.setPermissionId(permission);
        projectAcl.setProjectId(projectId);
        projectAcl.setEffect(effect);
        return projectAcl;
    }

    private ProjectAcl buildProjectAclUsingCurrentTenant(String identity, String permission, String resourceId, String effect) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(resourceId);
        return buildProjectAcl(identity, permission, projectId, effect);
    }

    @Override
    @Transactional
    public void revokePermission(ProjectAclDto projectAclDto) {
        //validate each permission is valid project permission.
        //validate identity is valid user and not *.
        //if resource is not * validate its a valid resource.
        //validate if permission is create-project or assign-create-project resource is *.

        String resourceId = projectAclDto.getResourceId();
        String identity = projectAclDto.getIdentity();
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(resourceId);

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId(AuthUtils.getCurrentTenantId());
        projectIdAll.setResourceId("*");

        Iterator<String> permissions = projectAclDto.getPermissions().iterator();
        while (permissions.hasNext()) {
            String permission = permissions.next();
            if (resourceId.equals("*")) {
                //find all acls where identity = input_identity and permission = input_permission
                long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndEffect(identity, permission, "ALLOW");
            } else {
                if (hasProjectPermission(identity, permission, projectIdAll)) {
                    ProjectAcl projectAclDenyAccess = buildProjectAcl(identity, permission, projectId, "DENY");
                    projectAclRepository.save(projectAclDenyAccess);
                } else {
                    long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndProjectIdAndEffect(identity, permission, projectId, "ALLOW");
                }
            }
        }
    }


    //to check if user has permission on a specific project (single not *).
    @Override
    @Transactional
    public boolean hasProjectPermission(String identity, String permission, ProjectId projectId) {
        //validate project id should be single instance only for create-project, assign-create-project it can be star.
        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId(projectId.getTenantId());
        projectIdAll.setResourceId("*");

        if (permission.equals("create-project") || permission.equals("assign-create-project")) {
            //deny entry wont be present for create-project or assign-create-project.
            //if you want to deny create-project or assign-create-project, remove the * entry for that user for that permission.
            //so effectively if user has entry for permission=create-project with effect=ALLOW on projectId=*, user has create-project permission.
            //and if user has entry for permission=assign-create-project with effect=ALLOW on projectId=*, user has assign-create-project permission.
            boolean isPermissionAllowedOnAllProjects = projectAclRepository.existsByIdentityAndPermissionIdAndEffectAndProjectId(identity, permission, "ALLOW", projectIdAll);
            return isPermissionAllowedOnAllProjects;
        } else {
            boolean isPermissionDeniedOnProject = projectAclRepository.existsByIdentityAndPermissionIdAndEffectAndProjectId(identity, permission, "DENY", projectId);
            if (isPermissionDeniedOnProject) {
                return false;
            }
            boolean isPermissionAllowedOnProject = projectAclRepository.existsByIdentityAndPermissionIdAndEffectAndProjectId(identity, permission, "ALLOW", projectId);
            boolean isPermissionAllowedOnAllProjects = projectAclRepository.existsByIdentityAndPermissionIdAndEffectAndProjectId(identity, permission, "ALLOW", projectIdAll);
            return isPermissionAllowedOnProject || isPermissionAllowedOnAllProjects;
        }
    }

    public boolean hasCreateProjectPermission(String identity, String tenantId) {
        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId(tenantId);
        projectIdAll.setResourceId("*");
        return hasProjectPermission(identity, "create-project", projectIdAll);
    }

    public boolean hasAssignCreateProjectPermission(String identity, String tenantId) {
        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId(tenantId);
        projectIdAll.setResourceId("*");
        return hasProjectPermission(identity, "assign-create-project", projectIdAll);
    }

    public Set<ProjectId> findAllUserProjects(String userName) {
        Set<ProjectId> projectIds1 = projectAclRepository.findAllProjectsByIdentityAndEffect(userName, "ALLOW");
        Set<ProjectId> projectIds2 = projectAclRepository.findAllProjectsByIdentityAndEffect(userName, "DENY");

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId(AuthUtils.getCurrentTenantId());
        projectIdAll.setResourceId("*");

        if (projectIds1.contains(projectIdAll)) {
            Set<ProjectId> allProjectIds = projectService.findAllProjectIds();
            allProjectIds.removeAll(projectIds2);
            return allProjectIds;
        } else {
            projectIds1.removeAll(projectIds2);
            return projectIds1;
        }
    }

    public Set<String> findAllProjectMembers(String resourceId) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(resourceId);

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId(AuthUtils.getCurrentTenantId());
        projectIdAll.setResourceId("*");

        List<ProjectId> projectIds1 = new ArrayList<>();
        projectIds1.add(projectId);
        projectIds1.add(projectIdAll);
        Set<String> membersAllowedAccess = projectAclRepository.findAllMembersByEffectAndProjectIdIn("ALLOW", projectIds1);

        List<ProjectId> projectIds2 = new ArrayList<>();
        projectIds2.add(projectId);
        Set<String> membersDeniedAccess = projectAclRepository.findAllMembersByEffectAndProjectIdIn("DENY", projectIds2);

        membersAllowedAccess.removeAll(membersDeniedAccess);
        return membersAllowedAccess;
    }

}
