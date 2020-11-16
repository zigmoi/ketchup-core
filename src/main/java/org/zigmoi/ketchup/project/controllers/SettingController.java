package org.zigmoi.ketchup.project.controllers;

import io.kubernetes.client.openapi.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.common.validations.ValidResourceId;
import org.zigmoi.ketchup.project.dtos.settings.*;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;
import org.zigmoi.ketchup.project.services.SettingService;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/v1-alpha/projects/{project-resource-id}")
public class SettingController {

    private static final Logger logger = LoggerFactory.getLogger(SettingController.class);

    @Autowired
    private Validator validator;

    @Autowired
    private SettingService settingService;

    @Autowired
    private PermissionUtilsService permissionUtilsService;

    /* container-registry api starts */
    @PostMapping("/container-registry-settings")
    @PreAuthorize("@permissionUtilsService.canPrincipalCreateSetting(#dto.projectResourceId)")
    public void createContainerRegistry(@RequestBody @Valid ContainerRegistrySettingsRequestDto dto) {
        settingService.createContainerRegistry(dto);
    }

    @GetMapping("/container-registry-settings")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public List<ContainerRegistrySettingsResponseDto> listAllContainerRegistry(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId) {
        return settingService.listAllContainerRegistry(projectResourceId);
    }

    @GetMapping("/container-registry-settings/{setting-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public ContainerRegistrySettingsResponseDto getContainerRegistry(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                                     @PathVariable("setting-resource-id") @ValidResourceId String settingResourceId) {
        return settingService.getContainerRegistry(projectResourceId, settingResourceId);
    }

    @PutMapping("/container-registry-settings/{setting-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void updateContainerRegistry(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                        @PathVariable("setting-resource-id") @ValidResourceId String settingResourceId,
                                        @RequestBody ContainerRegistrySettingsRequestDto dto) {
        Set<ConstraintViolation<ContainerRegistrySettingsRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        settingService.updateContainerRegistry(projectResourceId, settingResourceId, dto);
    }

    @DeleteMapping("/container-registry-settings/{setting-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteSetting(#projectResourceId)")
    public void deleteContainerRegistry(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                        @PathVariable("setting-resource-id") @ValidResourceId String settingResourceId) {
        settingService.deleteContainerRegistry(projectResourceId, settingResourceId);
    }
    /* container-registry api ends */


    /* kubernetes-cluster api starts */
    @PostMapping("/kubernetes-cluster-settings")
    @PreAuthorize("@permissionUtilsService.canPrincipalCreateSetting(#dto.projectResourceId)")
    public void createKubernetesCluster(@RequestBody @Valid KubernetesClusterSettingsRequestDto dto) {
        settingService.createKubernetesCluster(dto);
    }

    @GetMapping("/kubernetes-cluster-settings")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public List<KubernetesClusterSettingsResponseDto> listAllKubernetesCluster(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId) {
        return settingService.listAllKubernetesCluster(projectResourceId);
    }

    @GetMapping("/kubernetes-cluster-settings/{setting-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public KubernetesClusterSettingsResponseDto getKubernetesCluster(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                                     @PathVariable("setting-resource-id") @ValidResourceId String settingResourceId) {
        return settingService.getKubernetesCluster(projectResourceId, settingResourceId);
    }

    @PutMapping("/kubernetes-cluster-settings/{setting-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void updateKubernetesCluster(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                        @PathVariable("setting-resource-id") @ValidResourceId String settingResourceId,
                                        @RequestBody KubernetesClusterSettingsRequestDto dto) {
        Set<ConstraintViolation<KubernetesClusterSettingsRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        settingService.updateKubernetesCluster(projectResourceId, settingResourceId, dto);
    }

    @DeleteMapping("/kubernetes-cluster-settings/{setting-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteSetting(#projectResourceId)")
    public void deleteKubernetesCluster(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                        @PathVariable("setting-resource-id") @ValidResourceId String settingResourceId) {
        settingService.deleteKubernetesCluster(projectResourceId, settingResourceId);
    }

    @PostMapping("/kubernetes-cluster-settings/test-connection")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public Map<String, String> testKubernetesConnectivityAndAuthentication(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                                           @RequestBody KubernetesClusterSettingsRequestDto requestDto) {

        Set<ConstraintViolation<KubernetesClusterSettingsRequestDto>> violations = validator.validate(requestDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        boolean connectionSuccessful = false;
        try {
            String kubeConfig = StringUtility.decodeBase64(requestDto.getKubeconfig());
            connectionSuccessful = KubernetesUtility.testConnection(kubeConfig);
        } catch (Exception e) {
            connectionSuccessful = false;
        }
        Map<String, String> status = new HashMap<>();
        status.put("status", connectionSuccessful ? "success" : "failed");
        return status;
    }
    /* kubernetes-cluster api ends */


    /* build-tool api starts */
    @PostMapping("/build-tool-settings")
    @PreAuthorize("@permissionUtilsService.canPrincipalCreateSetting(#dto.projectResourceId)")
    public void createBuildTool(@RequestBody @Valid BuildToolSettingsRequestDto dto) {
        settingService.createBuildTool(dto);
    }

    @GetMapping("/build-tool-settings")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public List<BuildToolSettingsResponseDto> listAllBuildTool(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId) {
        return settingService.listAllBuildTool(projectResourceId);
    }

    @GetMapping("/build-tool-settings/{setting-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadSetting(#projectResourceId)")
    public BuildToolSettingsResponseDto getBuildTool(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                     @PathVariable("setting-resource-id") @ValidResourceId String settingResourceId) {
        return settingService.getBuildTool(projectResourceId, settingResourceId);
    }

    @PutMapping("/build-tool-settings/{setting-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateSetting(#projectResourceId)")
    public void updateBuildTool(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                @PathVariable("setting-resource-id") @ValidResourceId String settingResourceId,
                                @RequestBody BuildToolSettingsRequestDto dto) {
        Set<ConstraintViolation<BuildToolSettingsRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        settingService.updateBuildTool(projectResourceId, settingResourceId, dto);
    }

    @DeleteMapping("/build-tool-settings/{setting-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteSetting(#projectResourceId)")
    public void deleteBuildTool(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                @PathVariable("setting-resource-id") @ValidResourceId String settingResourceId) {
        settingService.deleteBuildTool(projectResourceId, settingResourceId);
    }
    /* build-tool api ends */


    /* k8s-host-alias api starts */
//    @PostMapping("/kubernetes-host-alias-settings")
//    public void createKubernetesHostAlias(@RequestBody @Valid KubernetesHostAliasSettingsRequestDto dto) {
//        settingService.createKubernetesHostAlias(dto);
//    }
//
//    @GetMapping("/kubernetes-host-alias-settings")
//    public List<KubernetesHostAliasSettingsResponseDto> listAllKubernetesHostAlias(@PathVariable("project-resource-id") String projectResourceId) {
//        return settingService.listAllKubernetesHostAlias(projectResourceId);
//    }
//
//    @GetMapping("/kubernetes-host-alias-settings/{setting-resource-id}")
//    public KubernetesHostAliasSettingsResponseDto getKubernetesHostAlias(@PathVariable("project-resource-id") String projectResourceId,
//                                                                         @PathVariable("setting-resource-id") String settingResourceId) {
//        return settingService.getKubernetesHostAlias(projectResourceId, settingResourceId);
//    }
//
//    @PutMapping("/kubernetes-host-alias-settings/{setting-resource-id}")
//    public void updateKubernetesHostAlias(@PathVariable("project-resource-id") String projectResourceId,
//                                          @PathVariable("setting-resource-id") String settingResourceId,
//                                          @RequestBody @Valid KubernetesHostAliasSettingsRequestDto dto) {
//        settingService.updateKubernetesHostAlias(projectResourceId, settingResourceId, dto);
//    }
//
//    @DeleteMapping("/kubernetes-host-alias-settings/{setting-resource-id}")
//    public void deleteKubernetesHostAlias(@PathVariable("project-resource-id") String projectResourceId,
//                                          @PathVariable("setting-resource-id") String settingResourceId) {
//        settingService.deleteKubernetesHostAlias(projectResourceId, settingResourceId);
//    }
    /* k8s-host-alias api ends */
}
