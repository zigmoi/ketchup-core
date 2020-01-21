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
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends TenantProviderService implements ProjectService {

    private final ProjectRepository projectRepository;

    private PermissionUtilsService permissionUtilsService;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, PermissionUtilsService permissionUtilsService) {
        this.projectRepository = projectRepository;
        this.permissionUtilsService = permissionUtilsService;
    }

    @Override
    @Transactional
    public void createProject(ProjectDto projectDto) {
        permissionUtilsService.validatePrincipalCanCreateProject(projectDto.getProjectResourceId());

        ProjectId projectId = new ProjectId();
        projectId.setResourceId(projectDto.getProjectResourceId());
        projectId.setTenantId(AuthUtils.getCurrentTenantId());

        if (projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s already exists.", projectDto.getProjectResourceId()));
        }

        Project project = new Project();
        project.setId(projectId);
        project.setDescription(projectDto.getDescription());
        project.setMembers(projectDto.getMembers());
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(String projectResourceId) {
        permissionUtilsService.validatePrincipalCanDeleteProject(projectResourceId);

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
        permissionUtilsService.validatePrincipalCanUpdateProjectDetails(projectResourceId);

        ProjectId projectId = new ProjectId();
        projectId.setResourceId(projectResourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        project.setDescription(description);
    }

    @Override
    @Transactional
    public void addMember(String projectResourceId, String member) {
        permissionUtilsService.validatePrincipalCanAddMember(projectResourceId);

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
        permissionUtilsService.validatePrincipalCanRemoveMember(projectResourceId);

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
        permissionUtilsService.validatePrincipalCanListMembers(projectResourceId);

        ProjectId projectId = new ProjectId();
        projectId.setResourceId(projectResourceId);
        projectId.setTenantId(AuthUtils.getCurrentTenantId());

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        return project.getMembers();
    }

    @Override
    public List<Project> listAllProjects() {
        Sort sort = new Sort(Sort.Direction.ASC, "id.resourceId");
        return projectRepository.findAll(sort)
                .stream()
                .filter(project -> permissionUtilsService.canPrincipalReadProjectDetails(project.getId().getResourceId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Project> findById(String projectResourceId) {
        permissionUtilsService.validatePrincipalCanReadProjectDetails(projectResourceId);

        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(projectResourceId);

        return projectRepository.findById(projectId);
    }

    @Override
    public boolean validateProject(ProjectId projectId) {
        if (projectId.getTenantId().equalsIgnoreCase(AuthUtils.getCurrentTenantId()) == false) {
            return false;
        }
        return projectRepository.existsById(projectId);
    }


}
