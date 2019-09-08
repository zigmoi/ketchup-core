package org.zigmoi.ketchup.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.authz.dtos.ProjectAclDto;
import org.zigmoi.ketchup.iam.authz.services.ProjectAclService;
import org.zigmoi.ketchup.iam.common.AuthUtils;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.repositories.ProjectRepository;
import org.zigmoi.ketchup.project.services.ProjectService;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

@RestController
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAclService projectAclService;

    @Autowired
    private ProjectService projectService;

    @PostMapping("/v1/project")
    public void createProject() {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        ProjectId projectIdAll = new ProjectId();
        projectIdAll.setTenantId(AuthUtils.getCurrentTenantId());
        projectIdAll.setResourceId("*");
        boolean canCreateProject = projectAclService.hasProjectPermission(identity, "create-project", projectIdAll);
        if (canCreateProject == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }

        Project p = new Project();
        ProjectId id = new ProjectId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setResourceId(UUID.randomUUID().toString());
        p.setId(id);
        p.setDescription("Test project.");
        projectRepository.save(p);
    }

    @GetMapping("/v1/project/{resourceId}")
    public Project getProject(@PathVariable("resourceId") String resourceId) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(resourceId);

        String identity = AuthUtils.getCurrentQualifiedUsername();
        boolean canReadProject = projectAclService.hasProjectPermission(identity, "read-project", projectId);
        if (canReadProject == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
        return projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
    }

    @PutMapping("/v1/project/assign/permissions")
    public void assignProjectPermissions(@RequestBody ProjectAclDto request) {
        //check tenant is current tenant.
        projectAclService.assignPermission(request);
    }

    @PutMapping("/v1/project/revoke/permissions")
    public void revokeProjectPermissions(@RequestBody ProjectAclDto request) {
        //check tenant is current tenant.
        projectAclService.revokePermission(request);
    }

    @GetMapping("/v1/project/{resourceId}/check/my/permission/{permissionId}")
    public boolean currentUserHasProjectPermission(@PathVariable("resourceId") String resourceId,
                                                   @PathVariable("permissionId") String permissionId, Principal principal) {
        String identity = principal.getName().toString();
        ProjectId projectId = new ProjectId();
        projectId.setResourceId(resourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        return projectAclService.hasProjectPermission(identity, permissionId, projectId);
    }

    @GetMapping("/v1/project/{resourceId}/check/user/{userName}/permission/{permissionId}")
    public boolean userHasProjectPermission(@PathVariable("resourceId") String resourceId,
                                            @PathVariable("userName") String userName,
                                            @PathVariable("permissionId") String permissionId) {
        return false;
    }

    @PutMapping("/v1/project/{resourceId}/member/{action}/{userName}")
    public void manageProjectMembers(@PathVariable("resourceId") String resourceId,
                                     @PathVariable("action") String action,
                                     @PathVariable("userName") String userName) {

        ProjectId projectId = new ProjectId();
        projectId.setResourceId(resourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());

        if ("ADD".equalsIgnoreCase(action)) {
            projectService.addMember(projectId, userName);
        } else if ("REMOVE".equalsIgnoreCase(action)) {
            projectService.removeMember(projectId, userName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action, allowed values are ADD and REMOVE.");
        }
    }

    @GetMapping("/v1/project/{resourceId}/members")
    public Set<String> listProjectMembers(@PathVariable("resourceId") String resourceId) {
        ProjectId projectId = new ProjectId();
        projectId.setResourceId(resourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        return projectService.listMembers(projectId);
    }

}
