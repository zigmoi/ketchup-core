package org.zigmoi.ketchup.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.authz.services.ProjectAclService;
import org.zigmoi.ketchup.iam.common.AuthUtils;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.repositories.ProjectRepository;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    private final UserService userService;

    private final ProjectAclService projectAclService;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, UserService userService, ProjectAclService projectAclService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.projectAclService = projectAclService;
    }


//    @Override
//    @Transactional
//    public boolean verifyMemberExists(ProjectId projectId, String member) {
//        return projectRepository.existsByIdAndMembersExists(projectId, member);
//    }

    @Override
    @Transactional
    public void addMember(ProjectId projectId, String member) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        canAddMember(identity, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        Set<String> members = project.getMembers();
        if (members.contains(member) == false) {
            members.add(member);
            project.setMembers(members);
            projectRepository.save(project);
        }
        userService.addProject(member, projectId);
    }

    @Override
    @Transactional
    public void removeMember(ProjectId projectId, String member) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        canRemoveMember(identity, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        Set<String> members = project.getMembers();
        if (members.contains(member)) {
            members.remove(member);
            project.setMembers(members);
            projectRepository.save(project);
        }
        userService.removeProject(member, projectId);
    }

    @Override
    @Transactional
    public Set<String> listMembers(ProjectId projectId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        canListMembers(identity, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        return project.getMembers();
    }

    @Override
    public Set<ProjectId> findAllProjectIds() {
        return projectRepository.findAllProjectIds();
    }

    @Override
    public Optional<Project> findById(ProjectId projectId) {
        return projectRepository.findById(projectId);
    }

    @Override
    public boolean validateProject(ProjectId projectId) {
        if (projectId.getTenantId().equalsIgnoreCase(AuthUtils.getCurrentTenantId()) == false) {
            return false;
        }
        return projectRepository.existsById(projectId);
    }

    private void canAddMember(String identity, ProjectId projectId) {
        boolean canCreateProject = projectAclService.hasProjectPermission(identity, "create-project", projectId);
        boolean canUpdateProject = projectAclService.hasProjectPermission(identity, "update-project", projectId);
        if (canCreateProject == false && canUpdateProject == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    private void canRemoveMember(String identity, ProjectId projectId) {
        boolean canDeleteProject = projectAclService.hasProjectPermission(identity, "delete-project", projectId);
        boolean canUpdateProject = projectAclService.hasProjectPermission(identity, "update-project", projectId);
        if (canDeleteProject == false && canUpdateProject == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    private void canListMembers(String identity, ProjectId projectId) {
        boolean canListProjectMembers = projectAclService.hasProjectPermission(identity, "list-project-members", projectId);
        if (canListProjectMembers == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }
}
