package org.zigmoi.ketchup.tenantsetting.services;

import org.springframework.web.multipart.MultipartFile;
import org.zigmoi.ketchup.tenantsetting.dtos.*;
import org.zigmoi.ketchup.tenantsetting.entities.*;

import java.util.List;
import java.util.Optional;

public interface TenantSettingsService {

    /* Git provider services */
    void createGitProvider(GitProviderSettingDto gitProviderSettingDto);

    List<GitProviderSettingEntity> listAllGitProviders();

    Optional<GitProviderSettingEntity> getGitProvider(String gitProviderId);

    void deleteGitProvider(String gitProviderId);

    /* Build tool services */
    void createBuildTool(BuildToolSettingDto buildToolSettingDto, MultipartFile file);

    List<BuildToolSettingEntity> listAllBuildTools();

    Optional<BuildToolSettingEntity> getBuildTool(String buildToolId);

    void deleteBuildTool(String buildToolId);

    /* Cloud credentials services */
    void createCloudProvider(CloudProviderSettingDto cloudProviderSettingDto);

    void deleteCloudCredential(String credentialId);

    /* Cloud registry services */
    void createCloudRegistry(CloudRegistrySettingDto cloudRegistrySettingDto);

    List<ContainerRegistrySettingEntity> listAllCloudRegistries();

    Optional<ContainerRegistrySettingEntity> getCloudRegistry(String registryId);

    void deleteCloudRegistry(String registryId);

    /* Cloud cluster services */
    void createCloudCluster(CloudClusterSettingDto cloudClusterSettingDto, MultipartFile file);

    List<CloudProviderSettingEntity> listAllCloudClusters();

    Optional<CloudProviderSettingEntity> getCloudCluster(String clusterId);

    void deleteCloudCluster(String clusterId);
}
