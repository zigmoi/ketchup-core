package org.zigmoi.ketchup.project.services;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.common.TransformUtility;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.project.dtos.settings.*;
import org.zigmoi.ketchup.project.entities.ProjectId;
import org.zigmoi.ketchup.project.entities.Setting;
import org.zigmoi.ketchup.project.entities.SettingId;
import org.zigmoi.ketchup.project.repositories.SettingRepository;

import java.util.*;

@Slf4j
@Service
public class SettingServiceImpl extends TenantProviderService implements SettingService {

    private final ProjectService projectService;
    private final PermissionUtilsService permissionUtilsService;
    private final SettingRepository settingRepository;

    @Autowired
    public SettingServiceImpl(ProjectService projectService,
                              PermissionUtilsService permissionUtilsService,
                              SettingRepository settingRepository) {
        this.projectService = projectService;
        this.permissionUtilsService = permissionUtilsService;
        this.settingRepository = settingRepository;
    }

    // container-registry api impl starts
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public List<ContainerRegistrySettingsResponseDto> listAllContainerRegistry(String projectResourceId) {
        List<ContainerRegistrySettingsResponseDto> settings = new ArrayList<>();
        for (Setting settingsEntity : settingRepository.findAllByProjectResourceIdAndType(projectResourceId,
                SettingType.CONTAINER_REGISTRY.toString())) {
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
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void createContainerRegistry(String projectResourceId, ContainerRegistrySettingsRequestDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(projectResourceId);
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        Setting settingsEntity = new Setting();
        SettingId settingId = new SettingId();
        settingId.setTenantId(AuthUtils.getCurrentTenantId());
        settingId.setProjectResourceId(projectResourceId);
        settingId.setSettingResourceId(getNewSettingId());
        settingsEntity.setId(settingId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(SettingType.CONTAINER_REGISTRY.toString());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public ContainerRegistrySettingsResponseDto getContainerRegistry(String projectResourceId, String settingResourceId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.CONTAINER_REGISTRY.toString());
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

    private void validateSettingType(Setting setting, String type) {
        if (type.equalsIgnoreCase(setting.getType()) == false) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Setting with id : %s not found.", setting.getSettingResourceId()));
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void updateContainerRegistry(String projectResourceId, String settingId, ContainerRegistrySettingsRequestDto dto) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.CONTAINER_REGISTRY.toString());
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteSetting(#projectResourceId)")
    public void deleteContainerRegistry(String projectResourceId, String settingResourceId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingResourceId, projectResourceId));
        }
        validateSettingType(settingsEntityOpt.get(), SettingType.CONTAINER_REGISTRY.toString());
        settingRepository.delete(settingsEntityOpt.get());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public long countAllContainerRegistryInProject(String projectResourceId) {
        return settingRepository.countAllByProjectResourceIdAndType(projectResourceId, SettingType.CONTAINER_REGISTRY.toString());
    }

    private void convertToDto(Setting settingsEntity, ContainerRegistrySettingsResponseDto settingsDto) {
        JSONObject jo = new JSONObject(settingsEntity.getData());
        settingsDto.setType(jo.getString("type"));
        settingsDto.setRegistryPassword(jo.getString("registryPassword"));
        settingsDto.setRegistryUsername(jo.getString("registryUsername"));
        settingsDto.setRegistryUrl(jo.getString("registryUrl"));
        settingsDto.setRepository(jo.getString("repository"));
        if(jo.has("redisUrl")){
            settingsDto.setRedisUrl(jo.getString("redisUrl"));
        }else{
            settingsDto.setRedisUrl("");
        }
        if(jo.has("redisPassword")){
            settingsDto.setRedisPassword(jo.getString("redisPassword"));
        }else{
            settingsDto.setRedisPassword("");
        }
    }

    private void convertToEntity(ContainerRegistrySettingsRequestDto settingsDto, Setting settingsEntity) {
        JSONObject jo = new JSONObject();
        jo.put("type", settingsDto.getType());
        jo.put("registryPassword", settingsDto.getRegistryPassword());
        jo.put("registryUsername", settingsDto.getRegistryUsername());
        jo.put("registryUrl", settingsDto.getRegistryUrl());
        jo.put("repository", settingsDto.getRepository());
        jo.put("redisUrl", settingsDto.getRedisUrl());
        jo.put("redisPassword", settingsDto.getRedisPassword());
        settingsEntity.setData(jo.toString());
    }
    // container-registry api impl ends


    // kubernetes-cluster api impl starts
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public List<KubernetesClusterSettingsResponseDto> listAllKubernetesCluster(String projectResourceId) {
        List<KubernetesClusterSettingsResponseDto> settings = new ArrayList<>();
        for (Setting settingsEntity : settingRepository.findAllByProjectResourceIdAndType(projectResourceId,
                SettingType.KUBERNETES_CLUSTER.toString())) {
            KubernetesClusterSettingsResponseDto settingsDto = new KubernetesClusterSettingsResponseDto();

            settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
            settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
            settingsDto.setDisplayName(settingsEntity.getDisplayName());
            settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
            settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
            settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
            settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
            convertToDto(settingsEntity, settingsDto);

            try {
                String devKubernetesBaseAddress = KubernetesUtility.getClusterIP(StringUtility.decodeBase64(settingsDto.getKubeconfig()));
                settingsDto.setBaseAddress(devKubernetesBaseAddress);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            settings.add(settingsDto);
        }
        return settings;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void createKubernetesCluster(String projectResourceId, KubernetesClusterSettingsRequestDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(projectResourceId);
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        Setting settingsEntity = new Setting();
        SettingId settingId = new SettingId();
        settingId.setTenantId(AuthUtils.getCurrentTenantId());
        settingId.setProjectResourceId(projectResourceId);
        settingId.setSettingResourceId(getNewSettingId());
        settingsEntity.setId(settingId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(SettingType.KUBERNETES_CLUSTER.toString());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public KubernetesClusterSettingsResponseDto getKubernetesCluster(String projectResourceId, String settingResourceId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.KUBERNETES_CLUSTER.toString());
        KubernetesClusterSettingsResponseDto settingsDto = new KubernetesClusterSettingsResponseDto();
        settingsDto.setProjectResourceId(settingsEntity.getProjectResourceId());
        settingsDto.setSettingResourceId(settingsEntity.getSettingResourceId());
        settingsDto.setDisplayName(settingsEntity.getDisplayName());
        settingsDto.setCreatedOn(settingsEntity.getCreatedOn());
        settingsDto.setCreatedBy(settingsEntity.getCreatedBy());
        settingsDto.setLastUpdatedOn(settingsEntity.getLastUpdatedOn());
        settingsDto.setLastUpdatedBy(settingsEntity.getLastUpdatedBy());
        convertToDto(settingsEntity, settingsDto);
        try {
            String devKubernetesBaseAddress = KubernetesUtility.getClusterIP(StringUtility.decodeBase64(settingsDto.getKubeconfig()));
            settingsDto.setBaseAddress(devKubernetesBaseAddress);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return settingsDto;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void updateKubernetesCluster(String projectResourceId, String settingResourceId, KubernetesClusterSettingsRequestDto dto) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.KUBERNETES_CLUSTER.toString());
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteSetting(#projectResourceId)")
    public void deleteKubernetesCluster(String projectResourceId, String settingResourceId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.KUBERNETES_CLUSTER.toString());
        settingRepository.delete(settingsEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public long countAllKubernetesClustersInProject(String projectResourceId) {
        return settingRepository.countAllByProjectResourceIdAndType(projectResourceId, SettingType.KUBERNETES_CLUSTER.toString());
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
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public List<BuildToolSettingsResponseDto> listAllBuildTool(String projectResourceId) {
        List<BuildToolSettingsResponseDto> settings = new ArrayList<>();
        for (Setting settingsEntity : settingRepository.findAllByProjectResourceIdAndType(projectResourceId,
                SettingType.BUILD_TOOL.toString())) {
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
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void createBuildTool(String projectResourceId, BuildToolSettingsRequestDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(projectResourceId);
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        Setting settingsEntity = new Setting();
        SettingId settingId = new SettingId();
        settingId.setTenantId(AuthUtils.getCurrentTenantId());
        settingId.setProjectResourceId(projectResourceId);
        settingId.setSettingResourceId(getNewSettingId());
        settingsEntity.setId(settingId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(SettingType.BUILD_TOOL.toString());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public BuildToolSettingsResponseDto getBuildTool(String projectResourceId, String settingResourceId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.BUILD_TOOL.toString());
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
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void updateBuildTool(String projectResourceId, String settingResourceId, BuildToolSettingsRequestDto dto) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.BUILD_TOOL.toString());
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteSetting(#projectResourceId)")
    public void deleteBuildTool(String projectResourceId, String settingResourceId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.BUILD_TOOL.toString());
        settingRepository.delete(settingsEntity);
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
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public List<KubernetesHostAliasSettingsResponseDto> listAllKubernetesHostAlias(String projectResourceId) {
        List<KubernetesHostAliasSettingsResponseDto> settings = new ArrayList<>();
        for (Setting settingsEntity : settingRepository.findAllByProjectResourceIdAndType(projectResourceId,
                SettingType.K8S_HOST_ALIAS.toString())) {
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
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#dto.projectResourceId)")
    public void createKubernetesHostAlias(String projectResourceId, KubernetesHostAliasSettingsRequestDto dto) {
        ProjectId projectId = new ProjectId();
        projectId.setTenantId(AuthUtils.getCurrentTenantId());
        projectId.setResourceId(projectResourceId);
        if (!projectService.validateProject(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Project with id %s not found.",
                    projectId.getResourceId()));
        }
        Setting settingsEntity = new Setting();
        SettingId settingId = new SettingId();
        settingId.setTenantId(AuthUtils.getCurrentTenantId());
        settingId.setProjectResourceId(projectResourceId);
        settingId.setSettingResourceId(getNewSettingId());
        settingsEntity.setId(settingId);
        settingsEntity.setDisplayName(dto.getDisplayName());
        settingsEntity.setType(SettingType.K8S_HOST_ALIAS.toString());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public KubernetesHostAliasSettingsResponseDto getKubernetesHostAlias(String projectResourceId,
                                                                         String settingResourceId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s not found.",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.K8S_HOST_ALIAS.toString());
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
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void updateKubernetesHostAlias(String projectResourceId, String settingResourceId, KubernetesHostAliasSettingsRequestDto dto) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.K8S_HOST_ALIAS.toString());
        settingsEntity.setDisplayName(dto.getDisplayName());
        convertToEntity(dto, settingsEntity);
        settingRepository.save(settingsEntity);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteSetting(#projectResourceId)")
    public void deleteKubernetesHostAlias(String projectResourceId, String settingResourceId) {
        Optional<Setting> settingsEntityOpt = settingRepository
                .findById(new SettingId(AuthUtils.getCurrentTenantId(), projectResourceId, settingResourceId));
        if (!settingsEntityOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Setting id : %s not found for Project : %s",
                            settingResourceId, projectResourceId));
        }
        Setting settingsEntity = settingsEntityOpt.get();
        validateSettingType(settingsEntity, SettingType.K8S_HOST_ALIAS.toString());
        settingRepository.delete(settingsEntity);
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
