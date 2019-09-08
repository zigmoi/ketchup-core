package org.zigmoi.ketchup.iam.authz.services;

import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.authz.dtos.ProjectAclDto;
import org.zigmoi.ketchup.iam.authz.entities.ProjectAcl;
import org.zigmoi.ketchup.iam.authz.exceptions.InvalidAclException;
import org.zigmoi.ketchup.iam.exceptions.CrossTenantOperationException;
=======
import org.springframework.stereotype.Service;
import org.zigmoi.ketchup.iam.authz.dtos.ProjectAclDto;
import org.zigmoi.ketchup.iam.authz.entities.ProjectAcl;
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.iam.authz.repositories.ProjectAclRepository;
import org.zigmoi.ketchup.iam.common.AuthUtils;
import org.zigmoi.ketchup.iam.services.UserService;
<<<<<<< HEAD
import org.zigmoi.ketchup.project.repositories.ProjectRepository;
=======
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
import org.zigmoi.ketchup.project.services.ProjectService;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class ProjectAclServiceImpl implements ProjectAclService {

<<<<<<< HEAD
    private final ProjectAclRepository projectAclRepository;

    //private final UserService userService;

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectAclServiceImpl(ProjectAclRepository projectAclRepository, ProjectRepository projectRepository) {
        this.projectAclRepository = projectAclRepository;
     //   this.userService = userService;
        this.projectRepository = projectRepository;
    }
=======
    @Autowired
    private ProjectAclRepository projectAclRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a

    @Override
    @Transactional
    public void assignPermission(ProjectAclDto projectAclDto) {
        //validate each permission is valid project permission.
<<<<<<< HEAD

        String currentUser = AuthUtils.getCurrentQualifiedUsername();
        String resourceId = projectAclDto.getResourceId();
        String identity = projectAclDto.getIdentity();

=======
        //validate identity is valid user and not *.
        //if resource is not * validate its a valid resource.
        //validate if permission is create-project or assign-create-project resource is *.

        String resourceId = projectAclDto.getResourceId();
        String identity = projectAclDto.getIdentity();
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(resourceId);

        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId(AuthUtils.getCurrentTenantId());
        projectIdAll.setResourceId("*");

<<<<<<< HEAD
      //  validateIdentity(identity);
        validateProject(projectId);
        validateUserHasAllRequiredPermissionsOnProject(currentUser, projectId, projectAclDto.getPermissions());

        Set<ProjectAcl> aclsToAdd = new HashSet<>();

        for (String permission : projectAclDto.getPermissions()) {
            if (hasProjectPermission(identity, permission, projectId)) {
                if (resourceId.equals("*")) {
                    //In case of checking permission on * hasPermission will be true if entry for * is present.
                    //Even if some entries for deny are present hasPermission will be true.
                    //So when assigning permission on * remove any deny entries for that identity and permission.
                    long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndEffect(identity, permission, "DENY");
                    System.out.println("deletedAclsCount: " + deletedAclsCount);
                }
            } else {
                if (resourceId.equals("*")) {
                    //delete all entries for that identity and permission.
                    //add one allow entry for identity and permission on *.
                    long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionId(identity, permission);
                    System.out.println("deletedAclsCount: " + deletedAclsCount);

                    ProjectAcl projectAclAllow = buildProjectAcl(identity, permission, projectId, "ALLOW");
                    aclsToAdd.add(projectAclAllow);
                } else {
                    //delete all deny entries for that identity, permission and specific projectId.
                    //add one allow entry for that identity, permission and specific projectId.
                    //if permission on * present just remove deny entry , no need to add allow entry on that specific project.
                    long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndProjectIdAndEffect(identity, permission, projectId, "DENY");
                    System.out.println("deletedAclsCount: " + deletedAclsCount);
                    if (hasProjectPermission(identity, permission, projectIdAll) == false) {
                        ProjectAcl projectAclAllow = buildProjectAcl(identity, permission, projectId, "ALLOW");
                        aclsToAdd.add(projectAclAllow);
                    }
                }

=======
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
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
            }
        }
        projectAclRepository.saveAll(aclsToAdd);
    }

<<<<<<< HEAD
=======
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

>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
    @Override
    @Transactional
    public void revokePermission(ProjectAclDto projectAclDto) {
        //validate each permission is valid project permission.
<<<<<<< HEAD
        String currentUser = AuthUtils.getCurrentQualifiedUsername();
        String resourceId = projectAclDto.getResourceId();
        String identity = projectAclDto.getIdentity();

=======
        //validate identity is valid user and not *.
        //if resource is not * validate its a valid resource.
        //validate if permission is create-project or assign-create-project resource is *.

        String resourceId = projectAclDto.getResourceId();
        String identity = projectAclDto.getIdentity();
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(resourceId);

<<<<<<< HEAD
       // validateIdentity(identity);
        validateProject(projectId);
        validateUserHasAllRequiredPermissionsOnProject(currentUser, projectId, projectAclDto.getPermissions());

        for (String permission : projectAclDto.getPermissions()) {
            if (resourceId.equals("*")) {
                //deleting all allow entries to remove permission from each and every resource.
                //removing extra deny entries as well.
                long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionId(identity, permission);
                System.out.println("deletedAclsCount: " + deletedAclsCount);
            } else {
                if (hasProjectPermission(identity, permission, projectId)) {
                    //if permission is present than either * entry is present without specific deny entry on projectId.
                    //or permission is present than either * entry is present without specific deny entry on projectId.

                    // add deny entry for that specific projectId for that identity and permission.
                    ProjectAcl projectAclDenyAccess = buildProjectAcl(identity, permission, projectId, "DENY");
                    projectAclRepository.save(projectAclDenyAccess);

                    //delete all extra allow entries except on *.
                    long deletedAclsCount = projectAclRepository.deleteAllByIdentityAndPermissionIdAndProjectIdAndEffect(identity, permission, projectId, "ALLOW");
                    System.out.println("deletedAclsCount: " + deletedAclsCount);
=======
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
>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
                }
            }
        }
    }

<<<<<<< HEAD
=======

>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
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

<<<<<<< HEAD
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

    public String getRequiredPermission(String permission) {
        //vaidate permission is valid project permission
        if (permission.startsWith("assign-")) {
            return permission;
        } else {
            return "assign-".concat(permission);
        }
    }

    public void validateUserHasAllRequiredPermissionsOnProject(String identity, ProjectId projectId, Set<String> permissions) {
        for (String permission : permissions) {
            //check if this permission exist in core permission table and is a project permission
            if (("create-project".equals(permission) || "assign-create-project".equals(permission)) && "*".equals(projectId.getResourceId()) == false) {
                //throw exception "create-project" and "assign-create-project" can only be assigned/revoked to *.
                throw new InvalidAclException(String.format("%s can only be assigned to * and not individual projects.", permission));
            }

            if (hasProjectPermission(identity, getRequiredPermission(permission), projectId) == false) {
                throw new AccessDeniedException("User does not have required permissions for this operation.");
            }
        }
    }

//    public void validateIdentity(String identity) {
//        //validate identity is valid user and not *.
//        String currentTenantId = AuthUtils.getCurrentTenantId();
//        if (identity.endsWith(currentTenantId) == false) {
//            throw new CrossTenantOperationException("Permissions can only be assigned to users of current tenant.");
//        }
//        userService.getUser(identity).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Identity should be a valid user."));
//    }

    public void validateProject(ProjectId projectId) {
        if (projectId.getTenantId().equalsIgnoreCase(AuthUtils.getCurrentTenantId()) == false) {
            throw new CrossTenantOperationException("");
        }
        if ("*".equalsIgnoreCase(projectId.getResourceId()) == false) {
            if (projectRepository.existsById(projectId) == false) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid project.");
            }
        }
    }


=======
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

>>>>>>> 7516c026e3957f51b7fb5836e18f8423d1ea584a
}
