package org.zigmoi.ketchup.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.project.dtos.*;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;
import org.zigmoi.ketchup.project.services.ProjectAclService;
import org.zigmoi.ketchup.project.services.ProjectService;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

@Validated
@RestController
@RequestMapping("/v1-alpha/projects")
public class ProjectController {

    @Autowired
    private Validator validator;

    @Autowired
    private ProjectAclService projectAclService;


    @Autowired
    private ProjectService projectService;

    @Autowired
    private PermissionUtilsService permissionUtilsService;

    @PostMapping
    @PreAuthorize("@permissionUtilsService.canPrincipalCreateProject(#projectDto.projectResourceId)")
    public void createProject(@Valid @RequestBody ProjectDto projectDto) {
        projectService.createProject(projectDto);
    }

    @DeleteMapping("/{project-name}")
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteProject(#projectName)")
    public void deleteProject(@PathVariable("project-name") @ValidProjectId String projectName) {
        projectService.deleteProject(projectName);
    }

    @PutMapping("/{project-name}")
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateProjectDetails(#projectName)")
    public void updateProject(@PathVariable("project-name") String projectName,
                              @RequestBody ProjectUpdateDto requestDto) {
        ProjectDto projectDto = new ProjectDto();
        projectDto.setProjectResourceId(projectName);
        projectDto.setDescription(requestDto.getDescription());
        Set<ConstraintViolation<ProjectDto>> violations = validator.validate(projectDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        projectService.updateProject(projectDto);
    }

    @GetMapping
    @PostFilter("@permissionUtilsService.canPrincipalReadProjectDetails(filterObject.getId().getResourceId())")
    public List<Project> listAllProjects() {
        return projectService.listAllProjects();
    }

    @GetMapping("/{project-name}")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadProjectDetails(#projectName)")
    public Project getProject(@PathVariable("project-name") @ValidProjectId String projectName) {
        return projectService.findById(projectName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
    }

    @PutMapping("/{project-name}/permissions/assign")
    public void assignProjectPermissions(@PathVariable("project-name") @NotBlank @Size(max = 20) String projectName,
                                         @RequestBody ProjectAclDto request) {
        //check tenant is current tenant.
        Set<ConstraintViolation<ProjectAclDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        projectAclService.assignPermission(request);
    }

    @PutMapping("/{project-name}/permissions/revoke")
    public void revokeProjectPermissions(@PathVariable("project-name") @NotBlank @Size(max = 20) String projectName,
                                         @RequestBody ProjectAclDto request) {
        //check tenant is current tenant.
        Set<ConstraintViolation<ProjectAclDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        projectAclService.revokePermission(request);
    }

    @GetMapping("/{project-name}/permissions/{permission-id}/validate/my")
    public boolean currentUserHasProjectPermission(@PathVariable("project-name") @NotBlank @Size(max = 20) String projectName,
                                                   @PathVariable("permission-id")
                                                   @NotBlank
                                                   @Pattern(regexp = "create-project|read-project|update-project|delete-project|" +
                                                           "assign-create-project|assign-read-project|assign-update-project|assign-delete-project")
                                                           String permissionId) {
        //user can check his own permissions in any project.
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return projectAclService.hasProjectPermission(identity, permissionId, projectName);
    }

    @GetMapping("/{project-name}/permissions/{permission-id}/validate/{username}")
    public boolean userHasProjectPermission(@PathVariable("project-name") @NotBlank @Size(max = 20) String projectName,
                                            @PathVariable("username") @NotBlank @Size(max = 100) String userName,
                                            @PathVariable("permission-id")
                                            @NotBlank
                                            @Pattern(regexp = "create-project|read-project|update-project|delete-project|" +
                                                    "assign-create-project|assign-read-project|assign-update-project|assign-delete-project")
                                                    String permissionId) {

        if (userName.equalsIgnoreCase(AuthUtils.getCurrentQualifiedUsername()) == false) {
            permissionUtilsService.validatePrincipalCanReadProjectDetails(projectName);
        }

        String identity = userName;
        return projectAclService.hasProjectPermission(identity, permissionId, projectName);
    }

    @GetMapping("/{project-name}/permissions/{username}")
    public List<ProjectPermissionStatusDto> getAllProjectPermissionsForUser(@PathVariable("project-name") @NotBlank @Size(max = 20) String projectName,
                                                                            @PathVariable("username") @NotBlank @Size(max = 100) String userName) {
        //if current user has read permissions in project he can check any users permissions in that project.
        //user can check his own permissions in any project.
        if (userName.equalsIgnoreCase(AuthUtils.getCurrentQualifiedUsername()) == false) {
            permissionUtilsService.validatePrincipalCanReadProjectDetails(projectName);
        }

        String identity = userName;
        List<String> projectPermissions = asList("create-project",
                "read-project", "update-project", "delete-project",
                "assign-create-project", "assign-read-project",
                "assign-update-project", "assign-delete-project");
        List<ProjectPermissionStatusDto> allPermissionStatus = new ArrayList<>();
        for (String permissionId : projectPermissions) {
            boolean result = projectAclService.hasProjectPermission(identity, permissionId, projectName);
            ProjectPermissionStatusDto permissionStatusDto = new ProjectPermissionStatusDto();
            permissionStatusDto.setStatus(result);
            permissionStatusDto.setPermission(permissionId);

            allPermissionStatus.add(permissionStatusDto);
        }
        return allPermissionStatus;
    }

}
