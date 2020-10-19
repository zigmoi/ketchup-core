package org.zigmoi.ketchup.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.project.dtos.ProjectAclDto;
import org.zigmoi.ketchup.project.dtos.ProjectDto;
import org.zigmoi.ketchup.project.dtos.ProjectPermissionStatusDto;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;
import org.zigmoi.ketchup.project.services.ProjectAclService;
import org.zigmoi.ketchup.project.services.ProjectService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@RestController
public class ProjectController {

    @Autowired
    private ProjectAclService projectAclService;


    @Autowired
    private ProjectService projectService;

    @Autowired
    private PermissionUtilsService permissionUtilsService;

    @PostMapping("/v1-alpha/project")
    public void createProject(@RequestBody ProjectDto projectDto) {
        projectService.createProject(projectDto);
    }

    @DeleteMapping("/v1-alpha/project/{projectName}")
    public void deleteProject(@PathVariable("projectName") String projectName) {
        projectService.deleteProject(projectName);
    }

    @PutMapping("/v1-alpha/project/{projectName}/{projectDescription}")
    public void updateDescription(@PathVariable("projectName") String projectName, @PathVariable("projectDescription") String description) {
        projectService.updateDescription(projectName, description);
    }

    @GetMapping("/v1-alpha/projects")
    public List<Project> listAllProjects() {
        return projectService.listAllProjects();
    }

    @GetMapping("/v1-alpha/project/{resourceId}")
    public Project getProject(@PathVariable("resourceId") String resourceId) {
        return projectService.findById(resourceId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
    }

    @PutMapping("/v1-alpha/project/assign/permissions")
    public void assignProjectPermissions(@RequestBody ProjectAclDto request) {
        //check tenant is current tenant.
        projectAclService.assignPermission(request);
    }

    @PutMapping("/v1-alpha/project/revoke/permissions")
    public void revokeProjectPermissions(@RequestBody ProjectAclDto request) {
        //check tenant is current tenant.
        projectAclService.revokePermission(request);
    }

    @GetMapping("/v1-alpha/project/{resourceId}/check/my/permission/{permissionId}")
    public boolean currentUserHasProjectPermission(@PathVariable("resourceId") String resourceId,
                                                   @PathVariable("permissionId") String permissionId) {
        //user can check his own permissions in any project.
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return projectAclService.hasProjectPermission(identity, permissionId, resourceId);
    }

    @GetMapping("/v1-alpha/project/{resourceId}/check/user/{userName}/permission/{permissionId}")
    public boolean userHasProjectPermission(@PathVariable("resourceId") String resourceId,
                                            @PathVariable("userName") String userName,
                                            @PathVariable("permissionId") String permissionId) {

        if (userName.equalsIgnoreCase(AuthUtils.getCurrentQualifiedUsername()) == false) {
            permissionUtilsService.validatePrincipalCanReadProjectDetails(resourceId);
        }

        String identity = userName;
        return projectAclService.hasProjectPermission(identity, permissionId, resourceId);
    }

    @GetMapping("/v1-alpha/project/{resourceId}/user/{userName}/permissions")
    public List<ProjectPermissionStatusDto> getAllProjectPermissionsForUser(@PathVariable("resourceId") String resourceId,
                                                                            @PathVariable("userName") String userName) {
        //if current user has read permissions in project he can check any users permissions in that project.
        //user can check his own permissions in any project.
        if (userName.equalsIgnoreCase(AuthUtils.getCurrentQualifiedUsername()) == false) {
            permissionUtilsService.validatePrincipalCanReadProjectDetails(resourceId);
        }

        String identity = userName;
        List<String> projectPermissions = asList("create-project",
                "read-project", "update-project", "delete-project",
                "assign-create-project", "assign-read-project",
                "assign-update-project", "assign-delete-project");
        List<ProjectPermissionStatusDto> allPermissionStatus = new ArrayList<>();
        for (String permissionId : projectPermissions) {
            boolean result = projectAclService.hasProjectPermission(identity, permissionId, resourceId);
            ProjectPermissionStatusDto permissionStatusDto = new ProjectPermissionStatusDto();
            permissionStatusDto.setStatus(result);
            permissionStatusDto.setPermission(permissionId);

            allPermissionStatus.add(permissionStatusDto);
        }
        return allPermissionStatus;
    }

}
