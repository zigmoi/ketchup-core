package org.zigmoi.ketchup.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.project.dtos.ProjectAclDto;

import java.util.HashSet;
import java.util.Set;

@Service
public class PermissionUtilsService {

    @Autowired
    private ProjectAclService projectAclService;

    public boolean hasAnyPermissions(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "read-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "update-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "create-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-read-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-update-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-delete-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-create-project", projectResourceId);

    }

    public void validateUserCanCheckPermissions(String identity, String projectResourceId) {
        //if current user has read or any one of the assign permissions in project p
        // he can check any users permissions in that project p.
        //user can check his own permissions in any project.
        if (identity.equalsIgnoreCase(AuthUtils.getCurrentQualifiedUsername())) {
            return;
        }
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "read-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-read-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-update-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-delete-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-create-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void revokeAllProjectPermissions(String identity, String projectResourceId) {
        ProjectAclDto projectAclDto = new ProjectAclDto();
        projectAclDto.setIdentity(identity);
        projectAclDto.setProjectResourceId(projectResourceId);
        Set<String> permissions = new HashSet<>();
        permissions.add("create-project");
        permissions.add("read-project");
        permissions.add("update-project");
        permissions.add("delete-project");
        permissions.add("assign-create-project");
        permissions.add("assign-read-project");
        permissions.add("assign-update-project");
        permissions.add("assign-delete-project");
        projectAclDto.setPermissions(permissions);
        projectAclService.revokePermission(projectAclDto);
    }

    public void deleteAllPermissionEntriesForProject(String projectResourceId) {
        projectAclService.deleteAllPermissionEntriesForProject(projectResourceId);
    }

    public void deleteAllPermissionEntriesForTenant() {
        projectAclService.deleteAllPermissionEntriesForTenant();
    }

    public void validateUserCanListPermissions(String identity, String projectResourceId) {
        //if current user has read or any one of the assign permissions in project p
        // he can check any users permissions in that project p.
        //user can check his own permissions in any project.
        if (identity.equalsIgnoreCase(AuthUtils.getCurrentQualifiedUsername())) {
            return;
        }
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "read-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-read-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-update-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-delete-project", projectResourceId) ||
                projectAclService.hasProjectPermission(identity, "assign-create-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validateUserCanCreateProject(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "create-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanCreateProject(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanCreateProject(identity, projectResourceId);
    }

    public boolean canUserCreateProject(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "create-project", projectResourceId);
    }

    public boolean canPrincipalCreateProject(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserCreateProject(identity, projectResourceId);
    }


    public void validateUserCanUpdateProjectDetails(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanUpdateProjectDetails(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanUpdateProjectDetails(identity, projectResourceId);
    }

    public boolean canUserUpdateProjectDetails(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
    }

    public boolean canPrincipalUpdateProjectDetails(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserUpdateProjectDetails(identity, projectResourceId);
    }


    public void validateUserCanReadProjectDetails(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanReadProjectDetails(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanReadProjectDetails(identity, projectResourceId);
    }

    public boolean canUserReadProjectDetails(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
    }

    public boolean canPrincipalReadProjectDetails(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserReadProjectDetails(identity, projectResourceId);
    }


    public void validateUserCanDeleteProject(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanDeleteProject(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanDeleteProject(identity, projectResourceId);
    }

    public boolean canUserDeleteProject(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
    }

    public boolean canPrincipalDeleteProject(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserDeleteProject(identity, projectResourceId);
    }


    public void validateUserCanCreateSetting(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanCreateSetting(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanCreateSetting(identity, projectResourceId);
    }

    public boolean canUserCreateSetting(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
    }

    public boolean canPrincipalCreateSetting(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserCreateSetting(identity, projectResourceId);
    }


    public void validateUserCanUpdateSetting(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanUpdateSetting(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanUpdateSetting(identity, projectResourceId);
    }

    public boolean canUserUpdateSetting(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
    }

    public boolean canPrincipalUpdateSetting(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserUpdateSetting(identity, projectResourceId);
    }


    public void validateUserCanReadSetting(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanReadSetting(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanReadSetting(identity, projectResourceId);
    }

    public boolean canUserReadSetting(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
    }

    public boolean canPrincipalReadSetting(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserReadSetting(identity, projectResourceId);
    }


    public void validateUserCanDeleteSetting(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanDeleteSetting(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanDeleteSetting(identity, projectResourceId);
    }

    public boolean canUserDeleteSetting(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
    }

    public boolean canPrincipalDeleteSetting(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserDeleteSetting(identity, projectResourceId);
    }


    public void validateUserCanCreateApplication(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanCreateApplication(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanCreateApplication(identity, projectResourceId);
    }

    public boolean canUserCreateApplication(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
    }

    public boolean canPrincipalCreateApplication(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserCreateApplication(identity, projectResourceId);
    }


    public void validateUserCanUpdateApplication(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanUpdateApplication(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanUpdateApplication(identity, projectResourceId);
    }

    public boolean canUserUpdateApplication(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
    }

    public boolean canPrincipalUpdateApplication(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserUpdateApplication(identity, projectResourceId);
    }


    public void validateUserCanReadApplication(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanReadApplication(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanReadApplication(identity, projectResourceId);
    }

    public boolean canUserReadApplication(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
    }

    public boolean canPrincipalReadApplication(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserReadApplication(identity, projectResourceId);
    }


    private void validateUserCanDeleteApplication(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanDeleteApplication(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanDeleteApplication(identity, projectResourceId);
    }

    public boolean canUserDeleteApplication(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
    }

    public boolean canPrincipalDeleteApplication(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserDeleteApplication(identity, projectResourceId);
    }

}
