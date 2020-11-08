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
    List<ContainerRegistrySettingsResponseDto> listAllContainerRegistry(@ValidProjectId String projectId);

    void createContainerRegistry(@Valid ContainerRegistrySettingsRequestDto dto);

    ContainerRegistrySettingsResponseDto getContainerRegistry(@ValidProjectId String projectId,
                                                              @ValidResourceId String settingId);

    void updateContainerRegistry(@ValidProjectId String projectId,
                                 @ValidResourceId String settingId,
                                 @Valid ContainerRegistrySettingsRequestDto dto);

    void deleteContainerRegistry(@ValidProjectId String projectId,
                                 @ValidResourceId String settingId);
    /* container-registry services ends */

    /* kubernetes-cluster services starts */
    List<KubernetesClusterSettingsResponseDto> listAllKubernetesCluster(@ValidProjectId String projectId);

    void createKubernetesCluster(@Valid KubernetesClusterSettingsRequestDto dto);

    KubernetesClusterSettingsResponseDto getKubernetesCluster(@ValidProjectId String projectId,
                                                              @ValidResourceId String settingId);

    void updateKubernetesCluster(@ValidProjectId String projectId,
                                 @ValidResourceId String settingId,
                                 @Valid KubernetesClusterSettingsRequestDto dto);

    void deleteKubernetesCluster(@ValidProjectId String projectId,
                                 @ValidResourceId String settingId);
    /* kubernetes-cluster services ends */

    /* build-tool services starts */
    List<BuildToolSettingsResponseDto> listAllBuildTool(@ValidProjectId String projectId);

    void createBuildTool(@Valid BuildToolSettingsRequestDto dto);

    BuildToolSettingsResponseDto getBuildTool(@ValidProjectId String projectId,
                                              @ValidResourceId String settingId);

    void updateBuildTool(@ValidProjectId String projectId,
                         @ValidResourceId String settingId,
                         @Valid BuildToolSettingsRequestDto dto);

    void deleteBuildTool(@ValidProjectId String projectId, @ValidResourceId String settingId);
    /* build-tool services ends */


    /* hostname-ip-mapping services starts */
    List<KubernetesHostAliasSettingsResponseDto> listAllKubernetesHostAlias(@ValidProjectId String projectId);

    void createKubernetesHostAlias(@Valid KubernetesHostAliasSettingsRequestDto dto);

    KubernetesHostAliasSettingsResponseDto getKubernetesHostAlias(@ValidProjectId String projectId,
                                                                  @ValidResourceId String settingId);

    void updateKubernetesHostAlias(@ValidProjectId String projectId,
                                   @ValidResourceId String settingId,
                                   @Valid KubernetesHostAliasSettingsRequestDto dto);

    void deleteKubernetesHostAlias(@ValidProjectId String projectId,
                                   @ValidResourceId String settingId);
    /* hostname-ip-mapping services ends */

}
