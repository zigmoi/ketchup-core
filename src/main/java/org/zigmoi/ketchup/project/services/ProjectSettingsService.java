package org.zigmoi.ketchup.project.services;

import org.zigmoi.ketchup.project.dtos.settings.*;

import java.util.List;

public interface ProjectSettingsService {

    /* container-registry services starts */
    List<ContainerRegistrySettingsResponseDto> listAllContainerRegistry(String projectId);
    void createContainerRegistry(ContainerRegistrySettingsRequestDto dto);
    ContainerRegistrySettingsResponseDto getContainerRegistry(String projectId, String settingId);
    void updateContainerRegistry(String projectId, String settingId, ContainerRegistrySettingsRequestDto dto);
    void deleteContainerRegistry(String projectId, String settingId);
    /* container-registry services ends */
    /* kubernetes-cluster services starts */
    List<KubernetesClusterSettingsResponseDto> listAllKubernetesCluster(String projectId);
    void createKubernetesCluster(KubernetesClusterSettingsRequestDto dto);
    KubernetesClusterSettingsResponseDto getKubernetesCluster(String projectId, String settingId);
    void updateKubernetesCluster(String projectId, String settingId, KubernetesClusterSettingsRequestDto dto);
    void deleteKubernetesCluster(String projectId, String settingId);
    /* kubernetes-cluster services ends */
    /* build-tool services starts */
    List<BuildToolSettingsResponseDto> listAllBuildTool(String projectId);
    void createBuildTool(BuildToolSettingsRequestDto dto);
    BuildToolSettingsResponseDto getBuildTool(String projectId, String settingId);
    void updateBuildTool(String projectId, String settingId, BuildToolSettingsRequestDto dto);
    void deleteBuildTool(String projectId, String settingId);
    /* build-tool services ends */
    /* git-provider services starts */
    List<GitProviderSettingsResponseDto> listAllGitProvider(String projectId);
    void createGitProvider(GitProviderSettingsRequestDto dto);
    GitProviderSettingsResponseDto getGitProvider(String projectId, String settingId);
    void updateGitProvider(String projectId, String settingId, GitProviderSettingsRequestDto dto);
    void deleteGitProvider(String projectId, String settingId);
    /* git-provider services ends */
    /* hostname-ip-mapping services starts */
    List<HostnameIpMappingSettingsResponseDto> listAllHostnameIpMapping(String projectId);
    void createHostnameIpMapping(HostnameIpMappingSettingsRequestDto dto);
    HostnameIpMappingSettingsResponseDto getHostnameIpMapping(String projectId, String settingId);
    void updateHostnameIpMapping(String projectId, String settingId, HostnameIpMappingSettingsRequestDto dto);
    void deleteHostnameIpMapping(String projectId, String settingId);
    /* hostname-ip-mapping services ends */
    /* cloud-provider services starts */
    List<CloudProviderSettingsResponseDto> listAllCloudProvider(String projectId);
    void createCloudProvider(CloudProviderSettingsRequestDto dto);
    CloudProviderSettingsResponseDto getCloudProvider(String projectId, String settingId);
    void updateCloudProvider(String projectId, String settingId, CloudProviderSettingsRequestDto dto);
    void deleteCloudProvider(String projectId, String settingId);
    /* cloud-provider services ends */
}
