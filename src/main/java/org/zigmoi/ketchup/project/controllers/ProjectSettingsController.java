package org.zigmoi.ketchup.project.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zigmoi.ketchup.project.dtos.settings.*;
import org.zigmoi.ketchup.project.services.ProjectSettingsService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
public class ProjectSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectSettingsController.class);

    @Autowired
    private ProjectSettingsService service;
    /* build-tool apis */

    @PostMapping("v1/settings/build-tool")
    public void createBuildTool(HttpServletRequest servletRequest,
                                @RequestBody @Valid BuildToolSettingsDto dto) {
        service.createBuildTool(dto);
    }

    @GetMapping("v1/settings/list-all-build-tool/{projectId}")
    public List<BuildToolSettingsDto> listAllBuildTool(HttpServletRequest servletRequest, @PathVariable("projectId")  String projectId) {
        return service.listAllBuildTool(projectId);
    }

    @GetMapping("v1/settings/build-tool/{projectId}/{id}")
    public Optional<BuildToolSettingsDto> getBuildTool(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                                       @PathVariable String projectId) {
        return service.getBuildTool(projectId, id);
    }

    @DeleteMapping("v1/settings/build-tool/{projectId}/{id}")
    public void deleteBuildTool(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                @PathVariable String projectId) {
        service.deleteBuildTool(projectId, id);
    }

    /* cloud-provider apis */

    @PostMapping("v1/settings/cloud-provider")
    public void createCloudProvider(HttpServletRequest servletRequest,
                                    @RequestBody @Valid CloudProviderSettingsDto dto) {
        service.createCloudProvider(dto);
    }

    @GetMapping("v1/settings/list-all-cloud-provider/{projectId}")
    public List<CloudProviderSettingsDto> listAllCloudProvider(HttpServletRequest servletRequest, @PathVariable("projectId")  String projectId) {
        return service.listAllCloudProvider(projectId);
    }

    @GetMapping("v1/settings/cloud-provider/{projectId}/{id}")
    public Optional<CloudProviderSettingsDto> getCloudProvider(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                                               @PathVariable String projectId) {
        return service.getCloudProvider(projectId, id);
    }

    @DeleteMapping("v1/settings/cloud-provider/{projectId}/{id}")
    public void deleteCloudProvider(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                    @PathVariable String projectId) {
        service.deleteCloudProvider(projectId, id);
    }

    /* container-registry apis */

    @PostMapping("v1/settings/container-registry")
    public void createContainerRegistry(HttpServletRequest servletRequest,
                                        @RequestBody @Valid ContainerRegistrySettingsDto dto) {
        service.createContainerRegistry(dto);
    }

    @GetMapping("v1/settings/list-all-container-registry/{projectId}")
    public List<ContainerRegistrySettingsDto> listAllContainerRegistry(HttpServletRequest servletRequest, @PathVariable("projectId")  String projectId) {
        return service.listAllContainerRegistry(projectId);
    }

    @GetMapping("v1/settings/container-registry/{projectId}/{id}")
    public Optional<ContainerRegistrySettingsDto> getContainerRegistry(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                                                       @PathVariable String projectId) {
        return service.getContainerRegistry(projectId, id);
    }

    @DeleteMapping("v1/settings/container-registry/{projectId}/{id}")
    public void deleteContainerRegistry(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                        @PathVariable String projectId) {
        service.deleteContainerRegistry(projectId, id);
    }

    /* git-provider apis */

    @PostMapping("v1/settings/git-provider")
    public void createGitProvider(HttpServletRequest servletRequest,
                                  @RequestBody @Valid GitProviderSettingsDto dto) {
        service.createGitProvider(dto);
    }

    @GetMapping("v1/settings/list-all-git-provider/{projectId}")
    public List<GitProviderSettingsDto> listAllGitProvider(HttpServletRequest servletRequest, @PathVariable("projectId")  String projectId) {
        return service.listAllGitProvider(projectId);
    }

    @GetMapping("v1/settings/git-provider/{projectId}/{id}")
    public Optional<GitProviderSettingsDto> getGitProvider(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                                           @PathVariable String projectId) {
        return service.getGitProvider(projectId, id);
    }

    @DeleteMapping("v1/settings/git-provider/{projectId}/{id}")
    public void deleteGitProvider(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                  @PathVariable String projectId) {
        service.deleteGitProvider(projectId, id);
    }

    /* hostname-ip-mapping apis */

    @PostMapping("v1/settings/hostname-ip-mapping")
    public void createHostnameIPMapping(HttpServletRequest servletRequest,
                                        @RequestBody @Valid HostnameIpMappingSettingsDto dto) {
        service.createHostnameIPMapping(dto);
    }

    @GetMapping("v1/settings/list-all-hostname-ip-mapping/{projectId}")
    public List<HostnameIpMappingSettingsDto> listAllHostnameIPMapping(HttpServletRequest servletRequest, @PathVariable("projectId")  String projectId) {
        return service.listAllHostnameIPMapping(projectId);
    }

    @GetMapping("v1/settings/hostname-ip-mapping/{projectId}/{id}")
    public Optional<HostnameIpMappingSettingsDto> getHostnameIPMapping(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                                                       @PathVariable String projectId) {
        return service.getHostnameIPMapping(projectId, id);
    }

    @DeleteMapping("v1/settings/hostname-ip-mapping/{projectId}/{id}")
    public void deleteHostnameIPMapping(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                        @PathVariable String projectId) {
        service.deleteHostnameIPMapping(projectId, id);
    }

    /* kubernetes-cluster apis */

    @PostMapping("v1/settings/kubernetes-cluster")
    public void createKubernetesCluster(HttpServletRequest servletRequest,
                                        @RequestBody @Valid KubernetesClusterSettingsDto dto) {
        service.createKubernetesCluster(dto);
    }

    @GetMapping("v1/settings/list-all-kubernetes-cluster/{projectId}")
    public List<KubernetesClusterSettingsDto> listAllKubernetesCluster(HttpServletRequest servletRequest, @PathVariable("projectId")  String projectId) {
        return service.listAllKubernetesCluster(projectId);
    }

    @GetMapping("v1/settings/kubernetes-cluster/{projectId}/{id}")
    public Optional<KubernetesClusterSettingsDto> getKubernetesCluster(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                                                       @PathVariable String projectId) {
        return service.getKubernetesCluster(projectId, id);
    }

    @DeleteMapping("v1/settings/kubernetes-cluster/{projectId}/{id}")
    public void deleteKubernetesCluster(HttpServletRequest servletRequest, @PathVariable("id") String id,
                                        @PathVariable String projectId) {
        service.deleteKubernetesCluster(projectId, id);
    }
}
