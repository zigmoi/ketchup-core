package org.zigmoi.ketchup.project.services;

import org.zigmoi.ketchup.project.dtos.settings.*;

import java.util.List;

public interface SettingService {

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


    /* hostname-ip-mapping services starts */
    List<KubernetesHostAliasSettingsResponseDto> listAllKubernetesHostAlias(String projectId);
    void createKubernetesHostAlias(KubernetesHostAliasSettingsRequestDto dto);
    KubernetesHostAliasSettingsResponseDto getKubernetesHostAlias(String projectId, String settingId);
    void updateKubernetesHostAlias(String projectId, String settingId, KubernetesHostAliasSettingsRequestDto dto);
    void deleteKubernetesHostAlias(String projectId, String settingId);
    /* hostname-ip-mapping services ends */

}
