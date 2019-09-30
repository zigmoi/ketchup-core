package org.zigmoi.ketchup.project.services;

import org.zigmoi.ketchup.project.dtos.settings.*;

import java.util.List;
import java.util.Optional;

public interface ProjectSettingsService {

    /* container-registry services starts */
    List<ContainerRegistrySettingsDto> listAllContainerRegistry(String projectId);
    void createContainerRegistry(ContainerRegistrySettingsDto dto);
    Optional<ContainerRegistrySettingsDto> getContainerRegistry(String projectId, String id);
    void deleteContainerRegistry(String projectId, String id);
    /* container-registry services ends */
    /* kubernetes-cluster services starts */
    List<KubernetesClusterSettingsDto> listAllKubernetesCluster(String projectId);
    void createKubernetesCluster(KubernetesClusterSettingsDto dto);
    Optional<KubernetesClusterSettingsDto> getKubernetesCluster(String projectId, String id);
    void deleteKubernetesCluster(String projectId, String id);
    /* kubernetes-cluster services ends */
    /* build-tool services starts */
    List<BuildToolSettingsDto> listAllBuildTool(String projectId);
    void createBuildTool(BuildToolSettingsDto dto);
    Optional<BuildToolSettingsDto> getBuildTool(String projectId, String id);
    void deleteBuildTool(String projectId, String id);
    /* build-tool services ends */
    /* git-provider services starts */
    List<GitProviderSettingsDto> listAllGitProvider(String projectId);
    void createGitProvider(GitProviderSettingsDto dto);
    Optional<GitProviderSettingsDto> getGitProvider(String projectId, String id);
    void deleteGitProvider(String projectId, String id);
    /* git-provider services ends */
    /* hostname-ip-mapping services starts */
    List<HostnameIpMappingSettingsDto> listAllHostnameIpMapping(String projectId);
    void createHostnameIpMapping(HostnameIpMappingSettingsDto dto);
    Optional<HostnameIpMappingSettingsDto> getHostnameIpMapping(String projectId, String id);
    void deleteHostnameIpMapping(String projectId, String id);
    /* hostname-ip-mapping services ends */
    /* cloud-provider services starts */
    List<CloudProviderSettingsDto> listAllCloudProvider(String projectId);
    void createCloudProvider(CloudProviderSettingsDto dto);
    Optional<CloudProviderSettingsDto> getCloudProvider(String projectId, String id);
    void deleteCloudProvider(String projectId, String id);
    /* cloud-provider services ends */
}
