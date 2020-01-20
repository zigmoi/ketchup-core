package org.zigmoi.ketchup.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.project.dtos.ProjectDto;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.repositories.ProjectRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProjectServiceImpl extends TenantProviderService implements ProjectService {

    private final ProjectRepository projectRepository;

    private final ProjectAclService projectAclService;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectAclService projectAclService) {
        this.projectRepository = projectRepository;
        this.projectAclService = projectAclService;
    }

    @Override
    @Transactional
    public void createProject(ProjectDto projectDto) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        String projectId = "*";
        boolean canCreateProject = projectAclService.hasProjectPermission(identity, "create-project", projectId);
        if (!canCreateProject) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }

        ProjectId projectId1 = new ProjectId();
        projectId1.setResourceId(projectDto.getProjectResourceId());
        projectId1.setTenantId(AuthUtils.getCurrentTenantId());

        if (projectRepository.existsById(projectId1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s already exists.", projectDto.getProjectResourceId()));
        }

        Project project = new Project();
        project.setId(projectId1);
        project.setDescription(projectDto.getDescription());
        project.setMembers(projectDto.getMembers());
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(String projectResourceId) {
        ProjectId projectId = new ProjectId();
        projectId.setResourceId(projectResourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());

        if (projectRepository.existsById(projectId) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.", projectResourceId));
        }
        projectRepository.deleteById(projectId);
    }

    @Override
    @Transactional
    public void updateDescription(String projectResourceId, String description) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanUpdateProjectDetails(identity, projectResourceId);

        ProjectId projectId = new ProjectId();
        projectId.setResourceId(projectResourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        project.setDescription(description);
    }

    @Override
    @Transactional
    public void addMember(String projectResourceId, String member) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanAddMember(identity, projectResourceId);

        ProjectId projectId = new ProjectId();
        projectId.setResourceId(projectResourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        Set<String> members = project.getMembers();
        if (members.contains(member) == false) {
            members.add(member);
            project.setMembers(members);
            projectRepository.save(project);
        }
    }

    @Override
    @Transactional
    public void removeMember(String projectResourceId, String member) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanRemoveMember(identity, projectResourceId);

        ProjectId projectId = new ProjectId();
        projectId.setResourceId(projectResourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        Set<String> members = project.getMembers();
        if (members.contains(member)) {
            members.remove(member);
            project.setMembers(members);
            projectRepository.save(project);
        }
    }

    @Override
    @Transactional
    public Set<String> listMembers(String projectResourceId) {
        String identity = AuthUtils.getCurrentQualifiedUsername();
        validateUserCanListMembers(identity, projectResourceId);

        ProjectId projectId = new ProjectId();
        projectId.setResourceId(projectResourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        return project.getMembers();
    }

    //@TenantFilter
    @Override
    public Set<ProjectId> findAllProjectIds() {
        return projectRepository.findAllProjectIds();
    }


    @Override
    public List<Project> listAllProjects() {
        Sort sort = new Sort(Sort.Direction.ASC, "id.resourceId");
        return projectRepository.findAll(sort);
    }

    @Override
    public Optional<Project> findById(String projectResourceId) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(projectResourceId);
        String identity = AuthUtils.getCurrentQualifiedUsername();
        boolean canReadProject = projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
        if (!canReadProject) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
        return projectRepository.findById(projectId);
    }

    @Override
    public boolean validateProject(ProjectId projectId) {
        if (projectId.getTenantId().equalsIgnoreCase(AuthUtils.getCurrentTenantId()) == false) {
            return false;
        }
        return projectRepository.existsById(projectId);
    }

    private void validateUserCanAddMember(String identity, String projectResourceId) {
        boolean canCreateProject = projectAclService.hasProjectPermission(identity, "create-project", projectResourceId);
        boolean canUpdateProject = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (canCreateProject == false && canUpdateProject == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    private void validateUserCanRemoveMember(String identity, String projectResourceId) {
        boolean canDeleteProject = projectAclService.hasProjectPermission(identity, "delete-project", projectResourceId);
        boolean canUpdateProject = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (canDeleteProject == false && canUpdateProject == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    private void validateUserCanListMembers(String identity, String projectResourceId) {
        boolean canListProjectMembers = projectAclService.hasProjectPermission(identity, "read-project", projectResourceId);
        if (canListProjectMembers == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }

    private void validateUserCanUpdateProjectDetails(String identity, String projectResourceId) {
        boolean canUpdateProject = projectAclService.hasProjectPermission(identity, "update-project", projectResourceId);
        if (canUpdateProject == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient privileges.");
        }
    }
}
