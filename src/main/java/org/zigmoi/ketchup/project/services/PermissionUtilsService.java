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
        permissions.add("");
        permissions.add("");
        permissions.add("");
        permissions.add("");
        permissions.add("");
        permissions.add("");
        permissions.add("");
        permissions.add("");
        projectAclDto.setPermissions(permissions);
        projectAclService.revokePermission(projectAclDto);
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


    public void validateUserCanAddMember(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanAddMember(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanAddMember(identity, projectResourceId);
    }

    public boolean canUserAddMember(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
    }

    public boolean canPrincipalAddMember(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserAddMember(identity, projectResourceId);
    }


    public void validateUserCanRemoveMember(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanRemoveMember(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanRemoveMember(identity, projectResourceId);
    }

    public boolean canUserRemoveMember(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
    }

    public boolean canPrincipalRemoveMember(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserRemoveMember(identity, projectResourceId);
    }


    public void validateUserCanListMembers(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanListMembers(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanListMembers(identity, projectResourceId);
    }

    public boolean canUserListMembers(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
    }

    public boolean canPrincipalListMembers(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserListMembers(identity, projectResourceId);
    }


    public void validateUserCanCreateDeployment(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanCreateDeployment(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanCreateDeployment(identity, projectResourceId);
    }

    public boolean canUserCreateDeployment(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
    }

    public boolean canPrincipalCreateDeployment(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserCreateDeployment(identity, projectResourceId);
    }


    public void validateUserCanUpdateDeployment(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanUpdateDeployment(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanUpdateDeployment(identity, projectResourceId);
    }

    public boolean canUserUpdateDeployment(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
    }

    public boolean canPrincipalUpdateDeployment(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserUpdateDeployment(identity, projectResourceId);
    }


    public void validateUserCanReadDeployment(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanReadDeployment(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanReadDeployment(identity, projectResourceId);
    }

    public boolean canUserReadDeployment(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
    }

    public boolean canPrincipalReadDeployment(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserReadDeployment(identity, projectResourceId);
    }


    private void validateUserCanDeleteDeployment(String identity, String projectResourceId) {
        boolean hasPermission = projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
        if (hasPermission == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    public void validatePrincipalCanDeleteDeployment(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanDeleteDeployment(identity, projectResourceId);
    }

    public boolean canUserDeleteDeployment(String identity, String projectResourceId) {
        return projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
    }

    public boolean canPrincipalDeleteDeployment(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        return canUserDeleteDeployment(identity, projectResourceId);
    }

}
