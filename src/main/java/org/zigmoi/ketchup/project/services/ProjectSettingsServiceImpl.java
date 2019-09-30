package org.zigmoi.ketchup.project.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.project.dtos.settings.*;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.entities.ProjectSettingsEntity;
import org.zigmoi.ketchup.project.entities.ProjectSettingsId;
import org.zigmoi.ketchup.project.repositories.ProjectSettingsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectSettingsServiceImpl extends TenantProviderService implements ProjectSettingsService {

    private final ProjectService projectService;
    private final ProjectAclService projectAclService;
    private final ProjectSettingsRepository projectSettingsRepository;

    @Autowired
    public ProjectSettingsServiceImpl(ProjectService projectService,
                                      ProjectAclService projectAclService,
                                      ProjectSettingsRepository projectSettingsRepository) {
        this.projectService = projectService;
        this.projectAclService = projectAclService;
        this.projectSettingsRepository = projectSettingsRepository;
    }

    @Override
    public List<BuildToolSettingsDto> listAllBuildTool(String projectId) {
        List<BuildToolSettingsDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.BUILD_TOOL.getType())) {
            BuildToolSettingsDto settingsDto = new BuildToolSettingsDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            convertToDto(settingsEntity, settingsDto);
        }
        return settings;
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, BuildToolSettingsDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setProvider(jo.getString("provider"));
        settingsDto.setFileName(jo.getString("fileName"));
        settingsDto.setFileData(jo.getString("fileData"));
    }

    @Override
    public void createBuildTool(BuildToolSettingsDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(dto.getSettingId());
    }

    @Override
    public Optional<BuildToolSettingsDto> getBuildTool(String projectId, String id) {
        return Optional.empty();
    }

    @Override
    public void deleteBuildTool(String projectId, String id) {

    }

    @Override
    public List<CloudProviderSettingsDto> listAllCloudProvider(String projectId) {
        return null;
    }

    @Override
    public void createCloudProvider(CloudProviderSettingsDto dto) {

    }

    @Override
    public Optional<CloudProviderSettingsDto> getCloudProvider(String projectId, String id) {
        return Optional.empty();
    }

    @Override
    public void deleteCloudProvider(String projectId, String id) {

    }

    @Override
    public List<ContainerRegistrySettingsDto> listAllContainerRegistry(String projectId) {
        return null;
    }

    @Override
    public void createContainerRegistry(ContainerRegistrySettingsDto dto) {

    }

    @Override
    public Optional<ContainerRegistrySettingsDto> getContainerRegistry(String projectId, String id) {
        return Optional.empty();
    }

    @Override
    public void deleteContainerRegistry(String projectId, String id) {

    }

    @Override
    public List<GitProviderSettingsDto> listAllGitProvider(String projectId) {
        return null;
    }

    @Override
    public void createGitProvider(GitProviderSettingsDto dto) {

    }

    @Override
    public Optional<GitProviderSettingsDto> getGitProvider(String projectId, String id) {
        return Optional.empty();
    }

    @Override
    public void deleteGitProvider(String projectId, String id) {

    }

    @Override
    public List<HostnameIpMappingSettingsDto> listAllHostnameIPMapping(String projectId) {
        return null;
    }

    @Override
    public void createHostnameIPMapping(HostnameIpMappingSettingsDto dto) {

    }

    @Override
    public Optional<HostnameIpMappingSettingsDto> getHostnameIPMapping(String projectId, String id) {
        return Optional.empty();
    }

    @Override
    public void deleteHostnameIPMapping(String projectId, String id) {

    }

    @Override
    public List<KubernetesClusterSettingsDto> listAllKubernetesCluster(String projectId) {
        return null;
    }

    @Override
    public void createKubernetesCluster(KubernetesClusterSettingsDto dto) {

    }

    @Override
    public Optional<KubernetesClusterSettingsDto> getKubernetesCluster(String projectId, String id) {
        return Optional.empty();
    }

    @Override
    public void deleteKubernetesCluster(String projectId, String id) {

    }
}
