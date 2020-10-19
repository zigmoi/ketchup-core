package org.zigmoi.ketchup.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.project.dtos.ProjectDto;
import org.zigmoi.ketchup.project.entities.Project;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.repositories.ProjectRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends TenantProviderService implements ProjectService {

    private final ProjectRepository projectRepository;

    private PermissionUtilsService permissionUtilsService;

    private UserService userService;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, PermissionUtilsService permissionUtilsService, UserService userService) {
        this.projectRepository = projectRepository;
        this.permissionUtilsService = permissionUtilsService;
        this.userService = userService;
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
