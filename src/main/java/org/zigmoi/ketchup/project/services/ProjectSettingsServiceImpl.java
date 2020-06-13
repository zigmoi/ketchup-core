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

import java.util.*;

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
    public List<ContainerRegistrySettingsResponseDto> listAllContainerRegistry(String projectId) {
        List<ContainerRegistrySettingsResponseDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.CONTAINER_REGISTRY.toString())){
            ContainerRegistrySettingsResponseDto settingsDto = new ContainerRegistrySettingsResponseDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
            settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
            settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
            settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
            convertToDto(settingsEntity, settingsDto);
            settings.add(settingsDto);
        }
        return settings;
    }

    @Override
    public void createContainerRegistry(ContainerRegistrySettingsRequestDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(getNewSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(ProjectSettingsType.CONTAINER_REGISTRY.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public ContainerRegistrySettingsResponseDto getContainerRegistry(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        ContainerRegistrySettingsResponseDto settingsDto = new ContainerRegistrySettingsResponseDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
        settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
        settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
        settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
        convertToDto(settingsEntity, settingsDto);
        return settingsDto;
    }

    @Override
    public void updateContainerRegistry(String projectId, String settingId, ContainerRegistrySettingsRequestDto dto) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public void deleteContainerRegistry(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, ContainerRegistrySettingsResponseDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setType(jo.getString("type"));
        settingsDto.setRegistryPassword(jo.getString("registryPassword"));
        settingsDto.setRegistryUsername(jo.getString("registryUsername"));
        settingsDto.setRegistryUrl(jo.getString("registryUrl"));
    }

    private void convertToEntity(ContainerRegistrySettingsRequestDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("type", settingsDto.getType());
        jo.put("registryPassword", settingsDto.getRegistryPassword());
        jo.put("registryUsername", settingsDto.getRegistryUsername());
        jo.put("registryUrl", settingsDto.getRegistryUrl());
        settingsEntity.setData(jo.toString());
    }
    // container-registry api impl ends


    // kubernetes-cluster api impl starts
    @Override
    public List<KubernetesClusterSettingsResponseDto> listAllKubernetesCluster(String projectId) {
        List<KubernetesClusterSettingsResponseDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.KUBERNETES_CLUSTER.toString())){
            KubernetesClusterSettingsResponseDto settingsDto = new KubernetesClusterSettingsResponseDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
            settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
            settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
            settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
            convertToDto(settingsEntity, settingsDto);
            settings.add(settingsDto);
        }
        return settings;
    }

    @Override
    public void createKubernetesCluster(KubernetesClusterSettingsRequestDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(getNewSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(ProjectSettingsType.KUBERNETES_CLUSTER.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public KubernetesClusterSettingsResponseDto getKubernetesCluster(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        KubernetesClusterSettingsResponseDto settingsDto = new KubernetesClusterSettingsResponseDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
        settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
        settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
        settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
        convertToDto(settingsEntity, settingsDto);
        return settingsDto;
    }

    @Override
    public void updateKubernetesCluster(String projectId, String settingId, KubernetesClusterSettingsRequestDto dto) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public void deleteKubernetesCluster(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, KubernetesClusterSettingsResponseDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
//        settingsDto.setProvider(jo.getString("provider"));
//        settingsDto.setFileName(jo.getString("fileName"));
        settingsDto.setFileData(jo.getString("fileData"));
    }

    private void convertToEntity(KubernetesClusterSettingsRequestDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
//        jo.put("provider", settingsDto.getProvider());
//        jo.put("fileName", settingsDto.getFileName());
        jo.put("fileData", settingsDto.getFileData());
        settingsEntity.setData(jo.toString());
    }
    // kubernetes-cluster api impl ends


    // build-tool api impl starts
    @Override
    public List<BuildToolSettingsResponseDto> listAllBuildTool(String projectId) {
        List<BuildToolSettingsResponseDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.BUILD_TOOL.toString())){
            BuildToolSettingsResponseDto settingsDto = new BuildToolSettingsResponseDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
            settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
            settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
            settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
            convertToDto(settingsEntity, settingsDto);
            settings.add(settingsDto);
        }
        return settings;
    }

    @Override
    public void createBuildTool(BuildToolSettingsRequestDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(getNewSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(ProjectSettingsType.BUILD_TOOL.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public BuildToolSettingsResponseDto getBuildTool(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        BuildToolSettingsResponseDto settingsDto = new BuildToolSettingsResponseDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
        settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
        settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
        settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
        convertToDto(settingsEntity, settingsDto);
        return settingsDto;
    }

    @Override
    public void updateBuildTool(String projectId, String settingId, BuildToolSettingsRequestDto dto) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public void deleteBuildTool(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, BuildToolSettingsResponseDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setType(jo.getString("type"));
//        settingsDto.setFileName(jo.getString("fileName"));
        settingsDto.setFileData(jo.getString("fileData"));
    }

    private void convertToEntity(BuildToolSettingsRequestDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("type", settingsDto.getType());
//        jo.put("fileName", settingsDto.getFileName());
        jo.put("fileData", settingsDto.getFileData());
        settingsEntity.setData(jo.toString());
    }
    // build-tool api impl ends


    // hostname-ip-mapping api impl starts
    @Override
    public List<K8sHostAliasSettingsResponseDto> listAllK8sHostAlias(String projectId) {
        List<K8sHostAliasSettingsResponseDto> settings = new ArrayList<>();
        for (ProjectSettingsEntity settingsEntity : projectSettingsRepository.findAllByIdProjectIdAndType(projectId,
                ProjectSettingsType.K8S_HOST_ALIAS.toString())){
            K8sHostAliasSettingsResponseDto settingsDto = new K8sHostAliasSettingsResponseDto();
            settingsDto.setProjectId(settingsEntity.getId().getProjectId());
            settingsDto.setSettingId(settingsEntity.getId().getSettingId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
            settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
            settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
            settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
            convertToDto(settingsEntity, settingsDto);
            settings.add(settingsDto);
        }
        return settings;
    }

    @Override
    public void createK8sHostAlias(K8sHostAliasSettingsRequestDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectId());
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        ProjectSettingsEntity settingsEntity = new ProjectSettingsEntity();
        ProjectSettingsId settingsId = new ProjectSettingsId();
        settingsId.setTenantId(AuthUtils.getCurrentTenantId());
        settingsId.setProjectId(dto.getProjectId());
        settingsId.setSettingId(getNewSettingId());
        settingsEntity.setId(settingsId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(ProjectSettingsType.K8S_HOST_ALIAS.toString());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public K8sHostAliasSettingsResponseDto getK8sHostAlias(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        K8sHostAliasSettingsResponseDto settingsDto = new K8sHostAliasSettingsResponseDto();
        settingsDto.setProjectId(settingsEntity.getId().getProjectId());
        settingsDto.setSettingId(settingsEntity.getId().getSettingId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
        settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
        settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
        settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
        convertToDto(settingsEntity, settingsDto);
        return settingsDto;
    }

    @Override
    public void updateK8sHostAlias(String projectId, String settingId, K8sHostAliasSettingsRequestDto dto) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        ProjectSettingsEntity settingsEntity = settingsEntityOpt.get();
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        projectSettingsRepository.save(settingsEntity);
    }

    @Override
    public void deleteK8sHostAlias(String projectId, String settingId) {
        Optional<ProjectSettingsEntity> settingsEntityOpt = projectSettingsRepository
                .findById(new ProjectSettingsId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        projectSettingsRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(ProjectSettingsEntity settingsEntity, K8sHostAliasSettingsResponseDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        if (jo.has("hostnameIpMapping")) {
            settingsDto.setHostnameIpMapping(TransformUtility.convertToMapStringString(new JSONObject(jo.getString("hostnameIpMapping")).toMap()));
        }
    }

    private void convertToEntity(K8sHostAliasSettingsRequestDto settingsDto, ProjectSettingsEntity settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("hostnameIpMapping", new JSONObject(settingsDto.getHostnameIpMapping()).toString());
        settingsEntity.setData(jo.toString());
    }
    // hostname-ip-mapping api impl ends

    private static String getNewSettingId() {
        return UUID.randomUUID().toString();
    }
}
