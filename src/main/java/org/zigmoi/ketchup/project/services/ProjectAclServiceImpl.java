package org.zigmoi.ketchup.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.exception.InvalidAclException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.exceptions.CrossTenantOperationException;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.project.dtos.ProjectAclDto;
import org.zigmoi.ketchup.project.entities.ProjectAcl;
import org.zigmoi.ketchup.project.entities.ProjectAclId;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.repositories.ProjectAclRepository;
import org.zigmoi.ketchup.project.repositories.ProjectRepository;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ProjectAclServiceImpl extends TenantProviderService implements ProjectAclService {

    private final ProjectAclRepository projectAclRepository;

    private final ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    @Autowired
    public ProjectAclServiceImpl(ProjectAclRepository projectAclRepository, ProjectRepository projectRepository) {
        this.projectAclRepository = projectAclRepository;
        this.projectRepository = projectRepository;
    }


    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void assignPermission(ProjectAclDto projectAclDto) {
        //validate each permission is valid project permission.

        String currentUser = AuthUtils.getCurrentQualifiedUsername();
        System.out.println("currentUser:" + currentUser);

        String identity = projectAclDto.getIdentity();
        String projectResourceId = projectAclDto.getProjectResourceId();
        String allProjectsResourceId = "*";

        validateIdentity(identity);
        validateProject(projectResourceId);
        validateUserHasAllRequiredPermissionsOnProject(currentUser, projectResourceId, projectAclDto.getPermissions());

        Set<ProjectAcl> aclsToAdd = new HashSet<>();

        for (String permission : projectAclDto.getPermissions()) {
            if (hasProjectPermission(identity, permission, projectResourceId)) {
                if (projectResourceId.equals("*")) {
                    //In case of checking permission on * hasPermission will be true if entry for * is present.
                    //Even if some entries for deny are present hasPermission will be true.
                    //So when assigning permission on * remove any deny entries for that identity and permission.
                    long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndEffect(identity, permission, "DENY");
                    System.out.println("deletedAclsCount: " + deletedAclsCount);
                }
            } else {
                if (projectResourceId.equals("*")) {
                    //delete all entries for that identity and permission.
                    //add one allow entry for identity and permission on *.
                    long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionId(identity, permission);
                    System.out.println("deletedAclsCount: " + deletedAclsCount);

                    ProjectAcl projectAclAllow = buildProjectAcl(identity, permission, projectResourceId, "ALLOW");
                    aclsToAdd.add(projectAclAllow);
                } else {
                    //delete all deny entries for that identity, permission and specific projectId.
                    //add one allow entry for that identity, permission and specific projectId.
                    //if permission on * present just remove deny entry , no need to add allow entry on that specific project.
                    System.out.println("deleting entries...");
                    long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndProjectResourceIdAndEffect(identity, permission, projectResourceId, "DENY");
                    System.out.println("deletedAclsCount: " + deletedAclsCount);
                    if (hasProjectPermission(identity, permission, allProjectsResourceId) == false) {
                        ProjectAcl projectAclAllow = buildProjectAcl(identity, permission, projectAclDto.getProjectResourceId(), "ALLOW");
                        aclsToAdd.add(projectAclAllow);
                    }
                }

            }
        }
        projectAclRepository.saveAll(aclsToAdd);
    }


    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void revokePermission(ProjectAclDto projectAclDto) {
        //validate each permission is valid project permission.
        String currentUser = AuthUtils.getCurrentQualifiedUsername();
        String identity = projectAclDto.getIdentity();
        String projectResourceId = projectAclDto.getProjectResourceId();

        validateIdentity(identity);
        validateProject(projectResourceId);
        validateUserHasAllRequiredPermissionsOnProject(currentUser, projectResourceId, projectAclDto.getPermissions());

        for (String permission : projectAclDto.getPermissions()) {
            if (projectResourceId.equals("*")) {
                //deleting all allow entries to remove permission from each and every resource.
                //removing extra deny entries as well.
                long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionId(identity, permission);
                System.out.println("deletedAclsCount: " + deletedAclsCount);
            } else {
                if (hasProjectPermission(identity, permission, projectResourceId)) {
                    //if permission is present than either * entry is present without specific deny entry on projectId.
                    //or permission is present than either * entry is present without specific deny entry on projectId.

                    // add deny entry for that specific projectId for that identity and permission.
                    ProjectAcl projectAclDenyAccess = buildProjectAcl(identity, permission, projectResourceId, "DENY");
                    projectAclRepository.save(projectAclDenyAccess);

                    //delete all extra allow entries except on *.
                    long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndProjectResourceIdAndEffect(identity, permission, projectResourceId, "ALLOW");
                    System.out.println("deletedAclsCount: " + deletedAclsCount);

                }
            }
        }
    }


    //checking permissions does not need any permissions,
    //from api point of view checks can be included in controller if required, to restrict users
    //from checking permissions of other users.

    //This function allows to check if user has permission on a specific project (single not *).
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public boolean hasProjectPermission(String identity, String permission, String projectResourceId) {
        //validate project id should be single instance only for create-project, assign-create-project it can be star.
        String allProjectsResourceId = "*";

        if (permission.equals("create-project") || permission.equals("assign-create-project")) {
            //deny entry wont be present for create-project or assign-create-project.
            //if you want to deny create-project or assign-create-project, remove the * entry for that user for that permission.
            //so effectively if user has entry for permission=create-project with effect=ALLOW on projectId=*, user has create-project permission.
            //and if user has entry for permission=assign-create-project with effect=ALLOW on projectId=*, user has assign-create-project permission.
            boolean isPermissionAllowedOnAllProjects = projectAclRepository.existsByIdentityAndPermissionIdAndEffectAndProjectResourceId(identity, permission, "ALLOW", allProjectsResourceId);
            return isPermissionAllowedOnAllProjects;
        } else {
            boolean isPermissionDeniedOnProject = projectAclRepository.existsByIdentityAndPermissionIdAndEffectAndProjectResourceId(identity, permission, "DENY", projectResourceId);
            if (isPermissionDeniedOnProject) {
                return false;
            }
            boolean isPermissionAllowedOnProject = projectAclRepository.existsByIdentityAndPermissionIdAndEffectAndProjectResourceId(identity, permission, "ALLOW", projectResourceId);
            boolean isPermissionAllowedOnAllProjects = projectAclRepository.existsByIdentityAndPermissionIdAndEffectAndProjectResourceId(identity, permission, "ALLOW", allProjectsResourceId);
            return isPermissionAllowedOnProject || isPermissionAllowedOnAllProjects;
        }
    }

    @Override
    @Transactional
    public void deleteAllPermissionEntriesForProject(String projectResourceId) {
        if(hasProjectPermission(AuthUtils.getCurrentQualifiedUsername(), "read-project", projectResourceId)){
            projectAclRepository.deleteAllEntriesForProject(projectResourceId);
        }else{
            throw new AccessDeniedException("User does not have required permissions for this operation.");
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TENANT_ADMIN')")
    public void deleteAllPermissionEntriesForTenant() {
            //super admin cannot delete these settings for specific tenant as it automatically gets current tenantId.
            projectAclRepository.deleteAllEntriesForTenant();
    }

    private ProjectAcl buildProjectAcl(String identity, String permission, String
            projectResourceId, String effect) {
        String aclId = UUID.randomUUID().toString();
        ProjectAcl projectAcl = new ProjectAcl();
        ProjectAclId projectAclId = new ProjectAclId();
        projectAclId.setTenantId(AuthUtils.getCurrentTenantId());
        projectAclId.setAclRuleId(aclId);
        projectAcl.setProjectAclId(projectAclId);
        projectAcl.setIdentity(identity);
        projectAcl.setPermissionId(permission);
        projectAcl.setProjectResourceId(projectResourceId);
        projectAcl.setEffect(effect);
        return projectAcl;
    }

    public String getRequiredPermission(String permission) {
        //vaidate permission is valid project permission
        if (permission.startsWith("assign-")) {
            return permission;
        } else {
            return "assign-".concat(permission);
        }
    }

    public void validateUserHasAllRequiredPermissionsOnProject(String identity, String
            projectResourceId, Set<String> permissions) {
        for (String permission : permissions) {
            //check if this permission exist in core permission table and is a project permission
            if (("create-project".equals(permission) || "assign-create-project".equals(permission)) && "*".equals(projectResourceId) == false) {
                //throw exception "create-project" and "assign-create-project" can only be assigned/revoked to *.
                throw new InvalidAclException(String.format("%s can only be assigned to * and not individual projects.", permission));
            }

            if (hasProjectPermission(identity, getRequiredPermission(permission), projectResourceId) == false) {
                throw new AccessDeniedException("User does not have required permissions for this operation.");
            }
        }
    }

    public void validateIdentity(String identity) {
        //validate identity is valid user and not *.
        String currentTenantId = AuthUtils.getCurrentTenantId();
        if (identity.endsWith(currentTenantId) == false) {
            throw new CrossTenantOperationException("Permissions can only be assigned to users of current tenant.");
        }
        userService.getUser(identity).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Identity should be a valid user."));
    }

    public void validateProject(String projectResourceId) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(projectResourceId);

        if ("*".equalsIgnoreCase(projectId.getResourceId()) == false) {
            if (projectRepository.existsById(projectId) == false) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid project.");
            }
        }
    }

}