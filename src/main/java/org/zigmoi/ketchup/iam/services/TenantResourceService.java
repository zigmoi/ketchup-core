package org.zigmoi.ketchup.iam.services;

import org.springframework.web.multipart.MultipartFile;
import org.zigmoi.ketchup.iam.dtos.*;
import org.zigmoi.ketchup.iam.entities.*;

import java.util.List;
import java.util.Optional;

public interface TenantResourceService {

    /* Git provider services */
    void createGitProvider(GitProviderDto gitProviderDto);

    List<GitProvider> listAllGitProviders();

    Optional<GitProvider> getGitProvider(String gitProviderId);

    void deleteGitProvider(String gitProviderId);

    /* Build tool services */
    void createBuildTool(BuildToolDto buildToolDto, MultipartFile file);

    List<BuildTool> listAllBuildTools();

    Optional<BuildTool> getBuildTool(String buildToolId);

    void deleteBuildTool(String buildToolId);

    /* Cloud credentials services */
    void createCloudCredential(CloudCredentialDto cloudCredentialDto);

    Optional<CloudCredential> getCloudCredential(String credentialId);

    List<CloudCredential> listAllCloudCredentials();

    void deleteCloudCredential(String credentialId);

    /* Cloud registry services */
    void createCloudRegistry(CloudRegistryDto cloudRegistryDto);

    List<CloudRegistry> listAllCloudRegistries();

    Optional<CloudRegistry> getCloudRegistry(String registryId);

    void deleteCloudRegistry(String registryId);

    /* Cloud cluster services */
    void createCloudCluster(CloudClusterDto cloudClusterDto, MultipartFile file);

    List<CloudCluster> listAllCloudClusters();

    Optional<CloudCluster> getCloudCluster(String clusterId);

    void deleteCloudCluster(String clusterId);
}