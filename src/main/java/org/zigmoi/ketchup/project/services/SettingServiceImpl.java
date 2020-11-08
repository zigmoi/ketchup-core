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
import org.zigmoi.ketchup.project.entities.Setting;
import org.zigmoi.ketchup.project.entities.SettingId;
import org.zigmoi.ketchup.project.repositories.SettingRepository;

import java.util.*;

@Service
public class SettingServiceImpl extends TenantProviderService implements SettingService {

    private final ProjectService projectService;
    private final ProjectAclService projectAclService;
    private final SettingRepository settingRepository;

    @Autowired
    public SettingServiceImpl(ProjectService projectService,
                              ProjectAclService projectAclService,
                              SettingRepository settingRepository) {
        this.projectService = projectService;
        this.projectAclService = projectAclService;
        this.settingRepository = settingRepository;
    }

    // container-registry api impl starts
    @Override
    public List<ContainerRegistrySettingsResponseDto> listAllContainerRegistry(String projectId) {
        List<ContainerRegistrySettingsResponseDto> settings = new ArrayList<>();
        for (Setting settingsEntity : settingRepository.findAllByProjectResourceIdAndType(projectId,
                SettingType.CONTAINER_REGISTRY.toString())){
            ContainerRegistrySettingsResponseDto settingsDto = new ContainerRegistrySettingsResponseDto();
            settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
            settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
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
        projectId.setResourceId(dto.getProjectResourceId());
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        Setting settingsEntity = new Setting();
        SettingId settingId = new SettingId();
        settingId.setTenantId(AuthUtils.getCurrentTenantId());
        settingId.setProjectResourceId(dto.getProjectResourceId());
        settingId.setSettingResourceId(getNewSettingId());
        settingsEntity.setId(settingId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(SettingType.CONTAINER_REGISTRY.toString());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    public ContainerRegistrySettingsResponseDto getContainerRegistry(String projectId, String settingId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        ContainerRegistrySettingsResponseDto settingsDto = new ContainerRegistrySettingsResponseDto();
        settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
        settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
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
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    public void deleteContainerRegistry(String projectId, String settingId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        settingRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(Setting settingsEntity, ContainerRegistrySettingsResponseDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setType(jo.getString("type"));
        settingsDto.setRegistryPassword(jo.getString("registryPassword"));
        settingsDto.setRegistryUsername(jo.getString("registryUsername"));
        settingsDto.setRegistryUrl(jo.getString("registryUrl"));
        settingsDto.setRepository(jo.getString("repository"));
    }

    private void convertToEntity(ContainerRegistrySettingsRequestDto settingsDto, Setting settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("type", settingsDto.getType());
        jo.put("registryPassword", settingsDto.getRegistryPassword());
        jo.put("registryUsername", settingsDto.getRegistryUsername());
        jo.put("registryUrl", settingsDto.getRegistryUrl());
        jo.put("repository", settingsDto.getRepository());
        settingsEntity.setData(jo.toString());
    }
    // container-registry api impl ends


    // kubernetes-cluster api impl starts
    @Override
    public List<KubernetesClusterSettingsResponseDto> listAllKubernetesCluster(String projectId) {
        List<KubernetesClusterSettingsResponseDto> settings = new ArrayList<>();
        for (Setting settingsEntity : settingRepository.findAllByProjectResourceIdAndType(projectId,
                SettingType.KUBERNETES_CLUSTER.toString())){
            KubernetesClusterSettingsResponseDto settingsDto = new KubernetesClusterSettingsResponseDto();
            settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
            settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
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
        projectId.setResourceId(dto.getProjectResourceId());
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        Setting settingsEntity = new Setting();
        SettingId settingId = new SettingId();
        settingId.setTenantId(AuthUtils.getCurrentTenantId());
        settingId.setProjectResourceId(dto.getProjectResourceId());
        settingId.setSettingResourceId(getNewSettingId());
        settingsEntity.setId(settingId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(SettingType.KUBERNETES_CLUSTER.toString());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    public KubernetesClusterSettingsResponseDto getKubernetesCluster(String projectId, String settingId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        KubernetesClusterSettingsResponseDto settingsDto = new KubernetesClusterSettingsResponseDto();
        settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
        settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
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
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    public void deleteKubernetesCluster(String projectId, String settingId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        settingRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(Setting settingsEntity, KubernetesClusterSettingsResponseDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
//        settingsDto.setProvider(jo.getString("provider"));
//        settingsDto.setFileName(jo.getString("fileName"));
        settingsDto.setKubeconfig(jo.getString("kubeconfig"));
    }

    private void convertToEntity(KubernetesClusterSettingsRequestDto settingsDto, Setting settingsEntity) {
        JSONObject jo = new JSONObject();
//        jo.put("provider", settingsDto.getProvider());
//        jo.put("fileName", settingsDto.getFileName());
        jo.put("kubeconfig", settingsDto.getKubeconfig());
        settingsEntity.setData(jo.toString());
    }
    // kubernetes-cluster api impl ends


    // build-tool api impl starts
    @Override
    public List<BuildToolSettingsResponseDto> listAllBuildTool(String projectId) {
        List<BuildToolSettingsResponseDto> settings = new ArrayList<>();
        for (Setting settingsEntity : settingRepository.findAllByProjectResourceIdAndType(projectId,
                SettingType.BUILD_TOOL.toString())){
            BuildToolSettingsResponseDto settingsDto = new BuildToolSettingsResponseDto();
            settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
            settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
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
        projectId.setResourceId(dto.getProjectResourceId());
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        Setting settingsEntity = new Setting();
        SettingId settingId = new SettingId();
        settingId.setTenantId(AuthUtils.getCurrentTenantId());
        settingId.setProjectResourceId(dto.getProjectResourceId());
        settingId.setSettingResourceId(getNewSettingId());
        settingsEntity.setId(settingId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(SettingType.BUILD_TOOL.toString());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    public BuildToolSettingsResponseDto getBuildTool(String projectId, String settingId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        BuildToolSettingsResponseDto settingsDto = new BuildToolSettingsResponseDto();
        settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
        settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
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
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    public void deleteBuildTool(String projectId, String settingId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        settingRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(Setting settingsEntity, BuildToolSettingsResponseDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setType(jo.getString("type"));
//        settingsDto.setFileName(jo.getString("fileName"));
        settingsDto.setFileData(jo.getString("fileData"));
    }

    private void convertToEntity(BuildToolSettingsRequestDto settingsDto, Setting settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("type", settingsDto.getType());
//        jo.put("fileName", settingsDto.getFileName());
        jo.put("fileData", settingsDto.getFileData());
        settingsEntity.setData(jo.toString());
    }
    // build-tool api impl ends


    // hostname-ip-mapping api impl starts
    @Override
    public List<KubernetesHostAliasSettingsResponseDto> listAllKubernetesHostAlias(String projectId) {
        List<KubernetesHostAliasSettingsResponseDto> settings = new ArrayList<>();
        for (Setting settingsEntity : settingRepository.findAllByProjectResourceIdAndType(projectId,
                SettingType.K8S_HOST_ALIAS.toString())){
            KubernetesHostAliasSettingsResponseDto settingsDto = new KubernetesHostAliasSettingsResponseDto();
            settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
            settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
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
    public void createKubernetesHostAlias(KubernetesHostAliasSettingsRequestDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(dto.getProjectResourceId());
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        Setting settingsEntity = new Setting();
        SettingId settingId = new SettingId();
        settingId.setTenantId(AuthUtils.getCurrentTenantId());
        settingId.setProjectResourceId(dto.getProjectResourceId());
        settingId.setSettingResourceId(getNewSettingId());
        settingsEntity.setId(settingId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(SettingType.K8S_HOST_ALIAS.toString());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    public KubernetesHostAliasSettingsResponseDto getKubernetesHostAlias(String projectId, String settingId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingId, projectId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        KubernetesHostAliasSettingsResponseDto settingsDto = new KubernetesHostAliasSettingsResponseDto();
        settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
        settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
        settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
        settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
        settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
        convertToDto(settingsEntity, settingsDto);
        return settingsDto;
    }

    @Override
    public void updateKubernetesHostAlias(String projectId, String settingId, KubernetesHostAliasSettingsRequestDto dto) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    public void deleteKubernetesHostAlias(String projectId, String settingId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectId));
        }
        settingRepository.delete(settingsEntityOpt.get());
    }

    private void convertToDto(Setting settingsEntity, KubernetesHostAliasSettingsResponseDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        if (jo.has("hostnameIpMapping")) {
            settingsDto.setHostnameIpMapping(TransformUtility.convertToMapStringString(new JSONObject(jo.getString("hostnameIpMapping")).toMap()));
        }
    }

    private void convertToEntity(KubernetesHostAliasSettingsRequestDto settingsDto, Setting settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("hostnameIpMapping", new JSONObject(settingsDto.getHostnameIpMapping()).toString());
        settingsEntity.setData(jo.toString());
    }
    // hostname-ip-mapping api impl ends

    private static String getNewSettingId() {
        return UUID.randomUUID().toString();
    }
}
