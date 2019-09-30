package org.zigmoi.ketchup.project.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.common.TransformUtility;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.project.dtos.settings.*;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.entities.ProjectSettingsEntity;
import org.zigmoi.ketchup.project.entities.ProjectSettingsId;
import org.zigmoi.ketchup.project.repositories.ProjectSettingsRepository;

import java.util.ArrayList;
import java.util.Date;
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

    // container-registry api impl starts
    @Override
    public List<ContainerRegistrySettingsDto> listAllContainerRegistry(String projectId) {
        List<ContainerRegistrySettingsDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.CONTAINER_REGISTRY.toString())){
            ContainerRegistrySettingsDto settingsDto = new ContainerRegistrySettingsDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            convertToDto(settingsEntity, settingsDto);
        }
        return settings;
    }

    @Override
    public void createContainerRegistry(ContainerRegistrySettingsDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(dto.getSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setCreatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setCreatedOn(new Date());
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setLastUpdatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setLastUpdatedOn(new Date());
        settingsEntity.setType(ProjectSettingsType.CONTAINER_REGISTRY.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public Optional<ContainerRegistrySettingsDto> getContainerRegistry(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        ContainerRegistrySettingsDto settingsDto = new ContainerRegistrySettingsDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        convertToDto(settingsEntity, settingsDto);
        return Optional.of(settingsDto);
    }

    @Override
    public void deleteContainerRegistry(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, ContainerRegistrySettingsDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setProvider(jo.getString("provider"));
        settingsDto.setCloudCredentialId(jo.getString("cloudCredentialId"));
        settingsDto.setRegistryId(jo.getString("registryId"));
        settingsDto.setRegistryUrl(jo.getString("registryUrl"));
    }

    private void convertToEntity(ContainerRegistrySettingsDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("provider", settingsDto.getProvider());
        jo.put("cloudCredentialId", settingsDto.getCloudCredentialId());
        jo.put("registryId", settingsDto.getRegistryId());
        jo.put("registryUrl", settingsDto.getRegistryUrl());
        settingsEntity.setData(jo.toString());
    }
    // container-registry api impl ends
    // kubernetes-cluster api impl starts
    @Override
    public List<KubernetesClusterSettingsDto> listAllKubernetesCluster(String projectId) {
        List<KubernetesClusterSettingsDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.KUBERNETES_CLUSTER.toString())){
            KubernetesClusterSettingsDto settingsDto = new KubernetesClusterSettingsDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            convertToDto(settingsEntity, settingsDto);
        }
        return settings;
    }

    @Override
    public void createKubernetesCluster(KubernetesClusterSettingsDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(dto.getSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setCreatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setCreatedOn(new Date());
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setLastUpdatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setLastUpdatedOn(new Date());
        settingsEntity.setType(ProjectSettingsType.KUBERNETES_CLUSTER.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public Optional<KubernetesClusterSettingsDto> getKubernetesCluster(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        KubernetesClusterSettingsDto settingsDto = new KubernetesClusterSettingsDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        convertToDto(settingsEntity, settingsDto);
        return Optional.of(settingsDto);
    }

    @Override
    public void deleteKubernetesCluster(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, KubernetesClusterSettingsDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setProvider(jo.getString("provider"));
        settingsDto.setFileName(jo.getString("fileName"));
        settingsDto.setFileData(jo.getString("fileData"));
    }

    private void convertToEntity(KubernetesClusterSettingsDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("provider", settingsDto.getProvider());
        jo.put("fileName", settingsDto.getFileName());
        jo.put("fileData", settingsDto.getFileData());
        settingsEntity.setData(jo.toString());
    }
    // kubernetes-cluster api impl ends
    // build-tool api impl starts
    @Override
    public List<BuildToolSettingsDto> listAllBuildTool(String projectId) {
        List<BuildToolSettingsDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.BUILD_TOOL.toString())){
            BuildToolSettingsDto settingsDto = new BuildToolSettingsDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            convertToDto(settingsEntity, settingsDto);
        }
        return settings;
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
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(dto.getSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setCreatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setCreatedOn(new Date());
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setLastUpdatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setLastUpdatedOn(new Date());
        settingsEntity.setType(ProjectSettingsType.BUILD_TOOL.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public Optional<BuildToolSettingsDto> getBuildTool(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        BuildToolSettingsDto settingsDto = new BuildToolSettingsDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        convertToDto(settingsEntity, settingsDto);
        return Optional.of(settingsDto);
    }

    @Override
    public void deleteBuildTool(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, BuildToolSettingsDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setProvider(jo.getString("provider"));
        settingsDto.setFileName(jo.getString("fileName"));
        settingsDto.setFileData(jo.getString("fileData"));
    }

    private void convertToEntity(BuildToolSettingsDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("provider", settingsDto.getProvider());
        jo.put("fileName", settingsDto.getFileName());
        jo.put("fileData", settingsDto.getFileData());
        settingsEntity.setData(jo.toString());
    }
    // build-tool api impl ends
    // git-provider api impl starts
    @Override
    public List<GitProviderSettingsDto> listAllGitProvider(String projectId) {
        List<GitProviderSettingsDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.GIT_PROVIDER.toString())){
            GitProviderSettingsDto settingsDto = new GitProviderSettingsDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            convertToDto(settingsEntity, settingsDto);
        }
        return settings;
    }

    @Override
    public void createGitProvider(GitProviderSettingsDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(dto.getSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setCreatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setCreatedOn(new Date());
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setLastUpdatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setLastUpdatedOn(new Date());
        settingsEntity.setType(ProjectSettingsType.GIT_PROVIDER.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public Optional<GitProviderSettingsDto> getGitProvider(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        GitProviderSettingsDto settingsDto = new GitProviderSettingsDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        convertToDto(settingsEntity, settingsDto);
        return Optional.of(settingsDto);
    }

    @Override
    public void deleteGitProvider(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, GitProviderSettingsDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setProvider(jo.getString("provider"));
        settingsDto.setRepoListUrl(jo.getString("repoListUrl"));
        settingsDto.setUsername(jo.getString("username"));
        settingsDto.setPassword(jo.getString("password"));
    }

    private void convertToEntity(GitProviderSettingsDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("provider", settingsDto.getProvider());
        jo.put("repoListUrl", settingsDto.getRepoListUrl());
        jo.put("username", settingsDto.getUsername());
        jo.put("password", settingsDto.getPassword());
        settingsEntity.setData(jo.toString());
    }
    // git-provider api impl ends
    // hostname-ip-mapping api impl starts
    @Override
    public List<HostnameIpMappingSettingsDto> listAllHostnameIpMapping(String projectId) {
        List<HostnameIpMappingSettingsDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.HOSTNAME_IP_MAPPING.toString())){
            HostnameIpMappingSettingsDto settingsDto = new HostnameIpMappingSettingsDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            convertToDto(settingsEntity, settingsDto);
        }
        return settings;
    }

    @Override
    public void createHostnameIpMapping(HostnameIpMappingSettingsDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(dto.getSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setCreatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setCreatedOn(new Date());
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setLastUpdatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setLastUpdatedOn(new Date());
        settingsEntity.setType(ProjectSettingsType.HOSTNAME_IP_MAPPING.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public Optional<HostnameIpMappingSettingsDto> getHostnameIpMapping(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        HostnameIpMappingSettingsDto settingsDto = new HostnameIpMappingSettingsDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        convertToDto(settingsEntity, settingsDto);
        return Optional.of(settingsDto);
    }

    @Override
    public void deleteHostnameIpMapping(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, HostnameIpMappingSettingsDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setHostnameIpMapping(TransformUtility.convertToMapStringString(jo.getJSONObject("hostnameIpMapping").toMap()));
    }

    private void convertToEntity(HostnameIpMappingSettingsDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("hostnameIpMapping", new JSONObject(settingsDto.getHostnameIpMapping()).toString());
        settingsEntity.setData(jo.toString());
    }
    // hostname-ip-mapping api impl ends
    // cloud-provider api impl starts
    @Override
    public List<CloudProviderSettingsDto> listAllCloudProvider(String projectId) {
        List<CloudProviderSettingsDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.CLOUD_PROVIDER.toString())){
            CloudProviderSettingsDto settingsDto = new CloudProviderSettingsDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            convertToDto(settingsEntity, settingsDto);
        }
        return settings;
    }

    @Override
    public void createCloudProvider(CloudProviderSettingsDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(dto.getSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setCreatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setCreatedOn(new Date());
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setLastUpdatedBy(AuthUtils.getCurrentUsername());
        settingsEntity.setLastUpdatedOn(new Date());
        settingsEntity.setType(ProjectSettingsType.CLOUD_PROVIDER.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public Optional<CloudProviderSettingsDto> getCloudProvider(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        CloudProviderSettingsDto settingsDto = new CloudProviderSettingsDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        convertToDto(settingsEntity, settingsDto);
        return Optional.of(settingsDto);
    }

    @Override
    public void deleteCloudProvider(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository.findById(new ProjectSettingsId(projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, CloudProviderSettingsDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setProvider(jo.getString("provider"));
        settingsDto.setAccessId(jo.getString("accessId"));
        settingsDto.setSecretKey(jo.getString("secretKey"));
    }

    private void convertToEntity(CloudProviderSettingsDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("provider", settingsDto.getProvider());
        jo.put("accessId", settingsDto.getAccessId());
        jo.put("secretKey", settingsDto.getSecretKey());
        settingsEntity.setData(jo.toString());
    }
    // cloud-provider api impl ends
}
