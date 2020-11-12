package org.zigmoi.ketchup.project.services;

import org.springframework.validation.annotation.Validated;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.common.validations.ValidResourceId;
import org.zigmoi.ketchup.project.dtos.settings.*;

import javax.validation.Valid;
import java.util.List;

@Validated
public interface SettingService {

    /* container-registry services starts */
    List<ContainerRegistrySettingsResponseDto> listAllContainerRegistry(@ValidProjectId String projectResourceId);

    void createContainerRegistry(@Valid ContainerRegistrySettingsRequestDto dto);

    ContainerRegistrySettingsResponseDto getContainerRegistry(@ValidProjectId String projectResourceId,
                                                              @ValidResourceId String settingResourceId);

    void updateContainerRegistry(@ValidProjectId String projectResourceId,
                                 @ValidResourceId String settingResourceId,
                                 @Valid ContainerRegistrySettingsRequestDto dto);

    void deleteContainerRegistry(@ValidProjectId String projectResourceId,
                                 @ValidResourceId String settingResourceId);

    long countAllContainerRegistryInProject(@ValidProjectId String projectResourceId);
    /* container-registry services ends */

    /* kubernetes-cluster services starts */
    List<KubernetesClusterSettingsResponseDto> listAllKubernetesCluster(@ValidProjectId String projectResourceId);

    void createKubernetesCluster(@Valid KubernetesClusterSettingsRequestDto dto);

    KubernetesClusterSettingsResponseDto getKubernetesCluster(@ValidProjectId String projectResourceId,
                                                              @ValidResourceId String settingResourceId);

    void updateKubernetesCluster(@ValidProjectId String projectResourceId,
                                 @ValidResourceId String settingResourceId,
                                 @Valid KubernetesClusterSettingsRequestDto dto);

    void deleteKubernetesCluster(@ValidProjectId String projectResourceId,
                                 @ValidResourceId String settingResourceId);

    long countAllKubernetesClustersInProject(@ValidProjectId String projectResourceId);
    /* kubernetes-cluster services ends */

    /* build-tool services starts */
    List<BuildToolSettingsResponseDto> listAllBuildTool(@ValidProjectId String projectResourceId);

    void createBuildTool(@Valid BuildToolSettingsRequestDto dto);

    BuildToolSettingsResponseDto getBuildTool(@ValidProjectId String projectResourceId,
                                              @ValidResourceId String settingResourceId);

    void updateBuildTool(@ValidProjectId String projectResourceId,
                         @ValidResourceId String settingResourceId,
                         @Valid BuildToolSettingsRequestDto dto);

    void deleteBuildTool(@ValidProjectId String projectResourceId, @ValidResourceId String settingResourceId);
    /* build-tool services ends */


    /* hostname-ip-mapping services starts */
    List<KubernetesHostAliasSettingsResponseDto> listAllKubernetesHostAlias(@ValidProjectId String projectResourceId);

    void createKubernetesHostAlias(@Valid KubernetesHostAliasSettingsRequestDto dto);

    KubernetesHostAliasSettingsResponseDto getKubernetesHostAlias(@ValidProjectId String projectResourceId,
                                                                  @ValidResourceId String settingResourceId);

    void updateKubernetesHostAlias(@ValidProjectId String projectResourceId,
                                   @ValidResourceId String settingResourceId,
                                   @Valid KubernetesHostAliasSettingsRequestDto dto);

    void deleteKubernetesHostAlias(@ValidProjectId String projectResourceId,
                                   @ValidResourceId String settingResourceId);
    /* hostname-ip-mapping services ends */

}
