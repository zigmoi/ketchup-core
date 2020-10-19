package org.zigmoi.ketchup.project.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zigmoi.ketchup.project.dtos.settings.*;
import org.zigmoi.ketchup.project.services.ProjectSettingsService;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ProjectSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectSettingsController.class);

    @Autowired
    ProjectSettingsService service;

    /* container-registry api starts */
    @PostMapping("/v1-alpha/settings/container-registry")
    public void createContainerRegistry(@RequestBody @Valid ContainerRegistrySettingsRequestDto dto) {
        service.createContainerRegistry(dto);
    }

    @GetMapping("/v1-alpha/settings/list-all-container-registry/{projectId}")
    public List<ContainerRegistrySettingsResponseDto> listAllContainerRegistry(@PathVariable("projectId")  String projectId) {
        return service.listAllContainerRegistry(projectId);
    }

    @GetMapping("/v1-alpha/settings/container-registry/{projectId}/{settingId}")
    public ContainerRegistrySettingsResponseDto getContainerRegistry(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getContainerRegistry(projectId, settingId);
    }

    @PutMapping("/v1-alpha/settings/container-registry/{projectId}/{settingId}")
    public void updateContainerRegistry(@PathVariable String settingId, @PathVariable String projectId, @RequestBody @Valid ContainerRegistrySettingsRequestDto dto) {
        service.updateContainerRegistry(projectId, settingId, dto);
    }

    @DeleteMapping("/v1-alpha/settings/container-registry/{projectId}/{settingId}")
    public void deleteContainerRegistry(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteContainerRegistry(projectId, settingId);
    }
    /* container-registry api ends */


    /* kubernetes-cluster api starts */
    @PostMapping("/v1-alpha/settings/kubernetes-cluster")
    public void createKubernetesCluster(@RequestBody @Valid KubernetesClusterSettingsRequestDto dto) {
        service.createKubernetesCluster(dto);
    }

    @GetMapping("/v1-alpha/settings/list-all-kubernetes-cluster/{projectId}")
    public List<KubernetesClusterSettingsResponseDto> listAllKubernetesCluster(@PathVariable("projectId")  String projectId) {
        return service.listAllKubernetesCluster(projectId);
    }

    @GetMapping("/v1-alpha/settings/kubernetes-cluster/{projectId}/{settingId}")
    public KubernetesClusterSettingsResponseDto getKubernetesCluster(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getKubernetesCluster(projectId, settingId);
    }

    @PutMapping("/v1-alpha/settings/kubernetes-cluster/{projectId}/{settingId}")
    public void updateKubernetesCluster(@PathVariable String settingId, @PathVariable String projectId, @RequestBody @Valid KubernetesClusterSettingsRequestDto dto) {
        service.updateKubernetesCluster(projectId, settingId, dto);
    }

    @DeleteMapping("/v1-alpha/settings/kubernetes-cluster/{projectId}/{settingId}")
    public void deleteKubernetesCluster(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteKubernetesCluster(projectId, settingId);
    }
    /* kubernetes-cluster api ends */


    /* build-tool api starts */
    @PostMapping("/v1-alpha/settings/build-tool")
    public void createBuildTool(@RequestBody @Valid BuildToolSettingsRequestDto dto) {
        service.createBuildTool(dto);
    }

    @GetMapping("/v1-alpha/settings/list-all-build-tool/{projectId}")
    public List<BuildToolSettingsResponseDto> listAllBuildTool(@PathVariable("projectId")  String projectId) {
        return service.listAllBuildTool(projectId);
    }

    @GetMapping("/v1-alpha/settings/build-tool/{projectId}/{settingId}")
    public BuildToolSettingsResponseDto getBuildTool(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getBuildTool(projectId, settingId);
    }

    @PutMapping("/v1-alpha/settings/build-tool/{projectId}/{settingId}")
    public void updateBuildTool(@PathVariable String settingId, @PathVariable String projectId, @RequestBody @Valid BuildToolSettingsRequestDto dto) {
        service.updateBuildTool(projectId, settingId, dto);
    }

    @DeleteMapping("/v1-alpha/settings/build-tool/{projectId}/{settingId}")
    public void deleteBuildTool(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteBuildTool(projectId, settingId);
    }
    /* build-tool api ends */


    /* k8s-host-alias api starts */
    @PostMapping("/v1-alpha/settings/k8s-host-alias")
    public void createK8sHostAlias(@RequestBody @Valid K8sHostAliasSettingsRequestDto dto) {
        service.createK8sHostAlias(dto);
    }

    @GetMapping("/v1-alpha/settings/list-all-k8s-host-alias/{projectId}")
    public List<K8sHostAliasSettingsResponseDto> listAllK8sHostAlias(@PathVariable("projectId")  String projectId) {
        return service.listAllK8sHostAlias(projectId);
    }

    @GetMapping("/v1-alpha/settings/k8s-host-alias/{projectId}/{settingId}")
    public K8sHostAliasSettingsResponseDto getK8sHostAlias(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getK8sHostAlias(projectId, settingId);
    }

    @PutMapping("/v1-alpha/settings/k8s-host-alias/{projectId}/{settingId}")
    public void updateK8sHostAlias(@PathVariable String settingId, @PathVariable String projectId, @RequestBody @Valid K8sHostAliasSettingsRequestDto dto) {
        service.updateK8sHostAlias(projectId, settingId, dto);
    }

    @DeleteMapping("/v1-alpha/settings/k8s-host-alias/{projectId}/{settingId}")
    public void deleteK8sHostAlias(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteK8sHostAlias(projectId, settingId);
    }
    /* k8s-host-alias api ends */
}
