package org.zigmoi.ketchup.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;
import org.zigmoi.ketchup.iam.authz.dtos.ProjectAclDto;
import org.zigmoi.ketchup.iam.authz.services.ProjectAclService;
import org.zigmoi.ketchup.iam.common.AuthUtils;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.repositories.ProjectRepository;

import java.security.Principal;
import java.util.UUID;

@RestController
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAclService projectAclService;

    @PostMapping("/v1/project")
    public void createProject() {
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
        ProjectId id = new ProjectId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setResourceId(resourceId);
        return projectRepository.findById(id).get();
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
}
