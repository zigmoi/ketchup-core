package org.zigmoi.ketchup.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.entities.CorePermissionMeta;
import org.zigmoi.ketchup.project.dtos.ProjectAclDto;
import org.zigmoi.ketchup.project.dtos.ProjectDto;
import org.zigmoi.ketchup.project.dtos.ProjectPermissionStatusDto;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.services.ProjectAclService;
import org.zigmoi.ketchup.project.services.ProjectService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@RestController
public class ProjectController {

    @Autowired
    private ProjectAclService projectAclService;


    @Autowired
    private ProjectService projectService;

    @PostMapping("/v1/project")
    public void createProject(@RequestBody ProjectDto projectDto) {
        projectService.createProject(projectDto);
    }

    @DeleteMapping("/v1/project/{projectName}")
    public void deleteProject(@PathVariable("projectName") String projectName) {
        projectService.deleteProject(projectName);
    }

    @PutMapping("/v1/project/{projectName}/{projectDescription}")
    public void updateDescription(@PathVariable("projectName") String projectName, @PathVariable("projectDescription") String description){
        projectService.updateDescription(projectName, description);
    }

    @GetMapping("/v1/projects")
    public List<Project> listAllProjects() {
        return projectService.listAllProjects();
    }

    @GetMapping("/v1/project/{resourceId}")
    public Project getProject(@PathVariable("resourceId") String resourceId) {
        return projectService.findById(resourceId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
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
                                                   @PathVariable("permissionId") String permissionId,
                                                   Principal principal) {
        String identity = principal.getName();
        return projectAclService.hasProjectPermission(identity, permissionId, resourceId);
    }

    @GetMapping("/v1/project/{resourceId}/check/user/{userName}/permission/{permissionId}")
    public boolean userHasProjectPermission(@PathVariable("resourceId") String resourceId,
                                            @PathVariable("userName") String userName,
                                            @PathVariable("permissionId") String permissionId,
                                            Principal principal) {
        String identity = userName;
        return projectAclService.hasProjectPermission(identity, permissionId, resourceId);
    }

    @GetMapping("/v1/project/{resourceId}/user/{userName}/permissions")
    public List<ProjectPermissionStatusDto> getAllProjectPermissionsForUser(@PathVariable("resourceId") String resourceId,
                                                                            @PathVariable("userName") String userName) {
        String identity = userName;
        List<String> projectPermissions = asList("create-project",
                "read-project", "update-project",
                "delete-project", "list-project-members",
                "assign-create-project", "assign-read-project",
                "assign-update-project", "assign-delete-project",
                "assign-list-project-members");
        List<ProjectPermissionStatusDto> allPermissionStatus = new ArrayList<>();
        for (String permissionId : projectPermissions) {

            boolean result = projectAclService.hasProjectPermission(identity, permissionId, resourceId);
            CorePermissionMeta corePermission = new CorePermissionMeta();
            corePermission.setPermissionId(permissionId);
            corePermission.setPermissionCategory("");
            corePermission.setPermissionDescription("");

            ProjectPermissionStatusDto permissionStatusDto = new ProjectPermissionStatusDto();
            permissionStatusDto.setStatus(result);
            permissionStatusDto.setPermission(corePermission);

            allPermissionStatus.add(permissionStatusDto);
        }
        return allPermissionStatus;
    }

    @PutMapping("/v1/project/{resourceId}/member/{action}/{userName}")
    public void manageProjectMembers(@PathVariable("resourceId") String resourceId,
                                     @PathVariable("action") String action,
                                     @PathVariable("userName") String userName) {

        if ("ADD".equalsIgnoreCase(action)) {
            projectService.addMember(resourceId, userName);
        } else if ("REMOVE".equalsIgnoreCase(action)) {
            projectService.removeMember(resourceId, userName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid action, allowed values are ADD and REMOVE.");
        }
    }

    @GetMapping("/v1/project/{resourceId}/members")
    public List<String> listProjectMembers(@PathVariable("resourceId") String resourceId) {
        return projectService.listMembers(resourceId)
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @PostMapping("/v1/project/{resourceId}/member/{memberName}/add")
    public void addProjectMember(@PathVariable("resourceId") String resourceId,
                                 @PathVariable("memberName") String memberName) {
        projectService.addMember(resourceId, memberName);
    }

    @PostMapping("/v1/project/{resourceId}/member/{memberName}/remove")
    public void removeProjectMember(@PathVariable("resourceId") String resourceId,
                                    @PathVariable("memberName") String memberName) {
        projectService.removeMember(resourceId, memberName);
    }
}
