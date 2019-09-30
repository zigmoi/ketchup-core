package org.zigmoi.ketchup.project.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zigmoi.ketchup.project.dtos.settings.*;
import org.zigmoi.ketchup.project.services.ProjectSettingsService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
public class ProjectSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectSettingsController.class);

    @Autowired
    ProjectSettingsService service;

    /* container-registry api starts */
    @PostMapping("v1/settings/container-registry")
    public void createContainerRegistry(@RequestBody @Valid ContainerRegistrySettingsDto dto) {
        service.createContainerRegistry(dto);
    }

    @GetMapping("v1/settings/list-all-container-registry/{projectId}")
    public List<ContainerRegistrySettingsDto> listAllContainerRegistry(@PathVariable("projectId")  String projectId) {
        return service.listAllContainerRegistry(projectId);
    }

    @GetMapping("v1/settings/container-registry/{projectId}/{settingId}")
    public Optional<ContainerRegistrySettingsDto> getContainerRegistry(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getContainerRegistry(projectId, settingId);
    }

    @DeleteMapping("v1/settings/container-registry/{projectId}/{settingId}")
    public void deleteContainerRegistry(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteContainerRegistry(projectId, settingId);
    }
    /* container-registry api ends */
    /* kubernetes-cluster api starts */
    @PostMapping("v1/settings/kubernetes-cluster")
    public void createKubernetesCluster(@RequestBody @Valid KubernetesClusterSettingsDto dto) {
        service.createKubernetesCluster(dto);
    }

    @GetMapping("v1/settings/list-all-kubernetes-cluster/{projectId}")
    public List<KubernetesClusterSettingsDto> listAllKubernetesCluster(@PathVariable("projectId")  String projectId) {
        return service.listAllKubernetesCluster(projectId);
    }

    @GetMapping("v1/settings/kubernetes-cluster/{projectId}/{settingId}")
    public Optional<KubernetesClusterSettingsDto> getKubernetesCluster(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getKubernetesCluster(projectId, settingId);
    }

    @DeleteMapping("v1/settings/kubernetes-cluster/{projectId}/{settingId}")
    public void deleteKubernetesCluster(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteKubernetesCluster(projectId, settingId);
    }
    /* kubernetes-cluster api ends */
    /* build-tool api starts */
    @PostMapping("v1/settings/build-tool")
    public void createBuildTool(@RequestBody @Valid BuildToolSettingsDto dto) {
        service.createBuildTool(dto);
    }

    @GetMapping("v1/settings/list-all-build-tool/{projectId}")
    public List<BuildToolSettingsDto> listAllBuildTool(@PathVariable("projectId")  String projectId) {
        return service.listAllBuildTool(projectId);
    }

    @GetMapping("v1/settings/build-tool/{projectId}/{settingId}")
    public Optional<BuildToolSettingsDto> getBuildTool(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getBuildTool(projectId, settingId);
    }

    @DeleteMapping("v1/settings/build-tool/{projectId}/{settingId}")
    public void deleteBuildTool(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteBuildTool(projectId, settingId);
    }
    /* build-tool api ends */
    /* git-provider api starts */
    @PostMapping("v1/settings/git-provider")
    public void createGitProvider(@RequestBody @Valid GitProviderSettingsDto dto) {
        service.createGitProvider(dto);
    }

    @GetMapping("v1/settings/list-all-git-provider/{projectId}")
    public List<GitProviderSettingsDto> listAllGitProvider(@PathVariable("projectId")  String projectId) {
        return service.listAllGitProvider(projectId);
    }

    @GetMapping("v1/settings/git-provider/{projectId}/{settingId}")
    public Optional<GitProviderSettingsDto> getGitProvider(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getGitProvider(projectId, settingId);
    }

    @DeleteMapping("v1/settings/git-provider/{projectId}/{settingId}")
    public void deleteGitProvider(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteGitProvider(projectId, settingId);
    }
    /* git-provider api ends */
    /* hostname-ip-mapping api starts */
    @PostMapping("v1/settings/hostname-ip-mapping")
    public void createHostnameIpMapping(@RequestBody @Valid HostnameIpMappingSettingsDto dto) {
        service.createHostnameIpMapping(dto);
    }

    @GetMapping("v1/settings/list-all-hostname-ip-mapping/{projectId}")
    public List<HostnameIpMappingSettingsDto> listAllHostnameIpMapping(@PathVariable("projectId")  String projectId) {
        return service.listAllHostnameIpMapping(projectId);
    }

    @GetMapping("v1/settings/hostname-ip-mapping/{projectId}/{settingId}")
    public Optional<HostnameIpMappingSettingsDto> getHostnameIpMapping(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getHostnameIpMapping(projectId, settingId);
    }

    @DeleteMapping("v1/settings/hostname-ip-mapping/{projectId}/{settingId}")
    public void deleteHostnameIpMapping(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteHostnameIpMapping(projectId, settingId);
    }
    /* hostname-ip-mapping api ends */
    /* cloud-provider api starts */
    @PostMapping("v1/settings/cloud-provider")
    public void createCloudProvider(@RequestBody @Valid CloudProviderSettingsDto dto) {
        service.createCloudProvider(dto);
    }

    @GetMapping("v1/settings/list-all-cloud-provider/{projectId}")
    public List<CloudProviderSettingsDto> listAllCloudProvider(@PathVariable("projectId")  String projectId) {
        return service.listAllCloudProvider(projectId);
    }

    @GetMapping("v1/settings/cloud-provider/{projectId}/{settingId}")
    public Optional<CloudProviderSettingsDto> getCloudProvider(@PathVariable String settingId, @PathVariable String projectId) {
        return service.getCloudProvider(projectId, settingId);
    }

    @DeleteMapping("v1/settings/cloud-provider/{projectId}/{settingId}")
    public void deleteCloudProvider(@PathVariable String settingId, @PathVariable String projectId) {
        service.deleteCloudProvider(projectId, settingId);
    }
    /* cloud-provider api ends */
}
