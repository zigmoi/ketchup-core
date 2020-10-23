package org.zigmoi.ketchup.project.controllers;

import io.kubernetes.client.openapi.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.project.dtos.settings.*;
import org.zigmoi.ketchup.project.services.SettingService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1-alpha/projects/{project-resource-id}")
public class SettingController {

    private static final Logger logger = LoggerFactory.getLogger(SettingController.class);

    @Autowired
    SettingService settingService;

    /* container-registry api starts */
    @PostMapping("/container-registry-settings")
    public void createContainerRegistry(@RequestBody @Valid ContainerRegistrySettingsRequestDto dto) {
        settingService.createContainerRegistry(dto);
    }

    @GetMapping("/container-registry-settings")
    public List<ContainerRegistrySettingsResponseDto> listAllContainerRegistry(@PathVariable("project-resource-id") String projectResourceId) {
        return settingService.listAllContainerRegistry(projectResourceId);
    }

    @GetMapping("/container-registry-settings/{setting-resource-id}")
    public ContainerRegistrySettingsResponseDto getContainerRegistry(@PathVariable("project-resource-id") String projectResourceId,
                                                                     @PathVariable("setting-resource-id") String settingResourceId) {
        return settingService.getContainerRegistry(projectResourceId, settingResourceId);
    }

    @PutMapping("/container-registry-settings/{setting-resource-id}")
    public void updateContainerRegistry(@PathVariable("project-resource-id") String projectResourceId,
                                        @PathVariable("setting-resource-id") String settingResourceId,
                                        @RequestBody @Valid ContainerRegistrySettingsRequestDto dto) {
        settingService.updateContainerRegistry(projectResourceId, settingResourceId, dto);
    }

    @DeleteMapping("/container-registry-settings/{setting-resource-id}")
    public void deleteContainerRegistry(@PathVariable("project-resource-id") String projectResourceId,
                                        @PathVariable("setting-resource-id") String settingResourceId) {
        settingService.deleteContainerRegistry(projectResourceId, settingResourceId);
    }
    /* container-registry api ends */


    /* kubernetes-cluster api starts */
    @PostMapping("/kubernetes-cluster-settings")
    public void createKubernetesCluster(@RequestBody @Valid KubernetesClusterSettingsRequestDto dto) {
        settingService.createKubernetesCluster(dto);
    }

    @GetMapping("/kubernetes-cluster-settings")
    public List<KubernetesClusterSettingsResponseDto> listAllKubernetesCluster(@PathVariable("project-resource-id") String projectResourceId) {
        return settingService.listAllKubernetesCluster(projectResourceId);
    }

    @GetMapping("/kubernetes-cluster-settings/{setting-resource-id}")
    public KubernetesClusterSettingsResponseDto getKubernetesCluster(@PathVariable("project-resource-id") String projectResourceId,
                                                                     @PathVariable("setting-resource-id") String settingResourceId) {
        return settingService.getKubernetesCluster(projectResourceId, settingResourceId);
    }

    @PutMapping("/kubernetes-cluster-settings/{setting-resource-id}")
    public void updateKubernetesCluster(@PathVariable("project-resource-id") String projectResourceId,
                                        @PathVariable("setting-resource-id") String settingResourceId,
                                        @RequestBody @Valid KubernetesClusterSettingsRequestDto dto) {
        settingService.updateKubernetesCluster(projectResourceId, settingResourceId, dto);
    }

    @DeleteMapping("/kubernetes-cluster-settings/{setting-resource-id}")
    public void deleteKubernetesCluster(@PathVariable("project-resource-id") String projectResourceId,
                                        @PathVariable("setting-resource-id") String settingResourceId) {
        settingService.deleteKubernetesCluster(projectResourceId, settingResourceId);
    }

    @PostMapping("/kubernetes-cluster-settings/test-connection")
    public Map<String, String> testKubernetesConnectivityAndAuthentication(@PathVariable("project-resource-id") String projectResourceId,
                                                                           @RequestBody KubernetesClusterSettingsRequestDto request) {
        boolean connectionSuccessful = false;
        try {
            String kubeConfig = StringUtility.decodeBase64(request.getFileData());
            connectionSuccessful = KubernetesUtility.testConnection(kubeConfig);
        } catch (ApiException | IOException e) {
            connectionSuccessful = false;
        }
        Map<String, String> status = new HashMap<>();
        status.put("status", connectionSuccessful ? "success" : "failed");
        return status;
    }
    /* kubernetes-cluster api ends */


    /* build-tool api starts */
    @PostMapping("/build-tool-settings")
    public void createBuildTool(@RequestBody @Valid BuildToolSettingsRequestDto dto) {
        settingService.createBuildTool(dto);
    }

    @GetMapping("/build-tool-settings")
    public List<BuildToolSettingsResponseDto> listAllBuildTool(@PathVariable("project-resource-id") String projectResourceId) {
        return settingService.listAllBuildTool(projectResourceId);
    }

    @GetMapping("/build-tool-settings/{setting-resource-id}")
    public BuildToolSettingsResponseDto getBuildTool(@PathVariable("project-resource-id") String projectResourceId,
                                                     @PathVariable("setting-resource-id") String settingResourceId) {
        return settingService.getBuildTool(projectResourceId, settingResourceId);
    }

    @PutMapping("/build-tool-settings/{setting-resource-id}")
    public void updateBuildTool(@PathVariable("project-resource-id") String projectResourceId,
                                @PathVariable("setting-resource-id") String settingResourceId,
                                @RequestBody @Valid BuildToolSettingsRequestDto dto) {
        settingService.updateBuildTool(projectResourceId, settingResourceId, dto);
    }

    @DeleteMapping("/build-tool-settings/{setting-resource-id}")
    public void deleteBuildTool(@PathVariable("project-resource-id") String projectResourceId,
                                @PathVariable("setting-resource-id") String settingResourceId) {
        settingService.deleteBuildTool(projectResourceId, settingResourceId);
    }
    /* build-tool api ends */


    /* k8s-host-alias api starts */
    @PostMapping("/kubernetes-host-alias-settings")
    public void createKubernetesHostAlias(@RequestBody @Valid KubernetesHostAliasSettingsRequestDto dto) {
        settingService.createKubernetesHostAlias(dto);
    }

    @GetMapping("/kubernetes-host-alias-settings")
    public List<KubernetesHostAliasSettingsResponseDto> listAllKubernetesHostAlias(@PathVariable("project-resource-id") String projectResourceId) {
        return settingService.listAllKubernetesHostAlias(projectResourceId);
    }

    @GetMapping("/kubernetes-host-alias-settings/{setting-resource-id}")
    public KubernetesHostAliasSettingsResponseDto getKubernetesHostAlias(@PathVariable("project-resource-id") String projectResourceId,
                                                                         @PathVariable("setting-resource-id") String settingResourceId) {
        return settingService.getKubernetesHostAlias(projectResourceId, settingResourceId);
    }

    @PutMapping("/kubernetes-host-alias-settings/{setting-resource-id}")
    public void updateKubernetesHostAlias(@PathVariable("project-resource-id") String projectResourceId,
                                          @PathVariable("setting-resource-id") String settingResourceId,
                                          @RequestBody @Valid KubernetesHostAliasSettingsRequestDto dto) {
        settingService.updateKubernetesHostAlias(projectResourceId, settingResourceId, dto);
    }

    @DeleteMapping("/kubernetes-host-alias-settings/{setting-resource-id}")
    public void deleteKubernetesHostAlias(@PathVariable("project-resource-id") String projectResourceId,
                                          @PathVariable("setting-resource-id") String settingResourceId) {
        settingService.deleteKubernetesHostAlias(projectResourceId, settingResourceId);
    }
    /* k8s-host-alias api ends */
}
