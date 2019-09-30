package org.zigmoi.ketchup.project.services;

import org.zigmoi.ketchup.project.dtos.settings.*;

import java.util.List;
import java.util.Optional;

public interface ProjectSettingsService {

    /* build-tool services */

    List<BuildToolSettingsDto> listAllBuildTool(String projectId);

    void createBuildTool(BuildToolSettingsDto dto);

    Optional<BuildToolSettingsDto> getBuildTool(String projectId, String id);

    void deleteBuildTool(String projectId, String id);

    /* cloud-provider services */

    List<CloudProviderSettingsDto> listAllCloudProvider(String projectId);

    void createCloudProvider(CloudProviderSettingsDto dto);

    Optional<CloudProviderSettingsDto> getCloudProvider(String projectId, String id);

    void deleteCloudProvider(String projectId, String id);

    /* container-registry services */

    List<ContainerRegistrySettingsDto> listAllContainerRegistry(String projectId);

    void createContainerRegistry(ContainerRegistrySettingsDto dto);

    Optional<ContainerRegistrySettingsDto> getContainerRegistry(String projectId, String id);

    void deleteContainerRegistry(String projectId, String id);

    /* git-provider services */

    List<GitProviderSettingsDto> listAllGitProvider(String projectId);

    void createGitProvider(GitProviderSettingsDto dto);

    Optional<GitProviderSettingsDto> getGitProvider(String projectId, String id);

    void deleteGitProvider(String projectId, String id);

    /* hostname-ip-mapping services */

    List<HostnameIpMappingSettingsDto> listAllHostnameIPMapping(String projectId);

    void createHostnameIPMapping(HostnameIpMappingSettingsDto dto);

    Optional<HostnameIpMappingSettingsDto> getHostnameIPMapping(String projectId, String id);

    void deleteHostnameIPMapping(String projectId, String id);

    /* kubernetes-cluster services */

    List<KubernetesClusterSettingsDto> listAllKubernetesCluster(String projectId);

    void createKubernetesCluster(KubernetesClusterSettingsDto dto);

    Optional<KubernetesClusterSettingsDto> getKubernetesCluster(String projectId, String id);

    void deleteKubernetesCluster(String projectId, String id);
}
