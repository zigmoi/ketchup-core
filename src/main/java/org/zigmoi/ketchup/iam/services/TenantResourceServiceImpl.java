package org.zigmoi.ketchup.iam.services;

import net.openhft.hashing.LongHashFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zigmoi.ketchup.iam.common.AuthUtils;
import org.zigmoi.ketchup.iam.dtos.*;
import org.zigmoi.ketchup.iam.entities.*;
import org.zigmoi.ketchup.iam.repositories.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantResourceServiceImpl implements TenantResourceService {

    @Autowired
    private GitProviderRepository gitProviderRepository;

    @Autowired
    private BuildToolRepository buildToolRepository;

    @Autowired
    private CloudCredentialRepository cloudCredentialRepository;

    @Autowired
    private CloudRegistryRepository cloudRegistryRepository;

    @Autowired
    private CloudClusterRepository cloudClusterRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /* Git provider services */
    @Override
    public void createGitProvider(@Valid GitProviderDto dto) {
        String uid = dto.getProvider() + "|" + dto.getDisplayName() + "|" + dto.getUsername()
                + "|" + dto.getRepoListUrl() + "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        GitProvider gitProvider = new GitProvider();
        gitProvider.setId(id);
        gitProvider.setProvider(dto.getProvider());
        gitProvider.setRepoListUrl(dto.getRepoListUrl());
        gitProvider.setDisplayName(dto.getDisplayName());
        gitProvider.setUsername(dto.getUsername());
        gitProvider.setPassword(passwordEncoder.encode(dto.getPassword()));
        gitProvider.setCreatedBy(AuthUtils.getCurrentUsername());
        gitProvider.setCreatedOn(new Date());
        gitProviderRepository.save(gitProvider);
    }

    @Override
    public List<GitProvider> listAllGitProviders() {
        return gitProviderRepository.findAllByTenantId(AuthUtils.getCurrentTenantId());
    }

    @Override
    public Optional<GitProvider> getGitProvider(String gitProviderId) {
        return gitProviderRepository.findByTenantIdAndId(AuthUtils.getCurrentTenantId(), gitProviderId);
    }

    @Override
    public void deleteGitProvider(String gitProviderId) {
        gitProviderRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), gitProviderId);
    }

    /* Build tool services */
    @Override
    public void createBuildTool(BuildToolDto buildToolDto, MultipartFile file) {
        String fileRemoteUrl = saveBuildToolFile(file);
        buildToolDto.setFileRemoteUrl(fileRemoteUrl);

        String uid = file.getName() + "|" + buildToolDto.getDisplayName() + "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        BuildTool buildTool = new BuildTool();
        buildTool.setId(id);
        buildTool.setProvider(buildToolDto.getProvider());
        buildTool.setDisplayName(buildToolDto.getDisplayName());
        buildTool.setFileName(buildToolDto.getFileName());
        buildTool.setFileRemoteUrl(buildToolDto.getFileRemoteUrl());
        buildTool.setCreatedBy(AuthUtils.getCurrentUsername());
        buildTool.setCreatedOn(new Date());
        buildToolRepository.save(buildTool);
    }

    private String saveBuildToolFile(MultipartFile file) { // TODO: 16/8/19 implement
        return file.getName();
    }

    @Override
    public List<BuildTool> listAllBuildTools() {
        return buildToolRepository.findAllByTenantId(AuthUtils.getCurrentTenantId());
    }

    @Override
    public Optional<BuildTool> getBuildTool(String buildToolId) {
        return buildToolRepository.findByTenantIdAndId(AuthUtils.getCurrentTenantId(), buildToolId);
    }

    @Override
    public void deleteBuildTool(String buildToolId) {
        buildToolRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), buildToolId);
    }

    /* Cloud credentials services */
    @Override
    public void createCloudCredential(CloudCredentialDto dto) {
        String uid = dto.getProvider() + "|" + dto.getDisplayName() + "|" + dto.getAccessId()
                + "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        CloudCredential credential = new CloudCredential();
        credential.setId(id);
        credential.setProvider(dto.getProvider());
        credential.setDisplayName(dto.getDisplayName());
        credential.setAccessId(dto.getAccessId());
        credential.setSecretKey(dto.getSecretKey());
        credential.setCreatedBy(AuthUtils.getCurrentUsername());
        credential.setCreatedOn(new Date());
        cloudCredentialRepository.save(credential);
    }

    @Override
    public List<CloudCredential> listAllCloudCredentials() {
        return cloudCredentialRepository.findAllByTenantId(AuthUtils.getCurrentTenantId());
    }

    @Override
    public Optional<CloudCredential> getCloudCredential(String credentialId) {
        return cloudCredentialRepository.findByTenantIdAndId(AuthUtils.getCurrentTenantId(), credentialId);
    }

    @Override
    public void deleteCloudCredential(String credentialId) {
        cloudCredentialRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), credentialId);
    }

    /* Cloud registry services */
    @Override
    public void createCloudRegistry(CloudRegistryDto dto) {
        String uid = dto.getProvider() + "|" + dto.getDisplayName() + "|" + dto.getRegistryId()
                + "|" + dto.getRegistryUrl() + "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        CloudRegistry registry = new CloudRegistry();
        registry.setId(id);
        registry.setProvider(dto.getProvider());
        registry.setDisplayName(dto.getDisplayName());
        registry.setCloudCredentialId(dto.getCloudCredentialId());
        registry.setRegistryId(dto.getRegistryId());
        registry.setRegistryUrl(dto.getRegistryUrl());
        registry.setCreatedBy(AuthUtils.getCurrentUsername());
        registry.setCreatedOn(new Date());
        cloudRegistryRepository.save(registry);
    }

    @Override
    public List<CloudRegistry> listAllCloudRegistries() {
        return cloudRegistryRepository.findAllByTenantId(AuthUtils.getCurrentTenantId());
    }

    @Override
    public Optional<CloudRegistry> getCloudRegistry(String registryId) {
        return cloudRegistryRepository.findByTenantIdAndId(AuthUtils.getCurrentTenantId(), registryId);
    }

    @Override
    public void deleteCloudRegistry(String registryId) {
        cloudRegistryRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), registryId);
    }

    /* cloud cluster services */
    @Override
    public void createCloudCluster(CloudClusterDto dto, MultipartFile file) {
        String fileRemoteUrl = saveCloudClusterFile(file);
        String uid = dto.getProvider() + "|" + dto.getDisplayName() + "|" + dto.getFileName()
                + "|" + fileRemoteUrl+ "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        CloudCluster cluster = new CloudCluster();
        cluster.setId(id);
        cluster.setProvider(dto.getProvider());
        cluster.setDisplayName(dto.getDisplayName());
        cluster.setFileName(dto.getFileName());
        cluster.setFileRemoteUrl(dto.getFileRemoteUrl());
        cluster.setCreatedBy(AuthUtils.getCurrentUsername());
        cluster.setCreatedOn(new Date());
        cloudClusterRepository.save(cluster);
    }

    private String saveCloudClusterFile(MultipartFile file) {
        return file.getName();
    }

    @Override
    public List<CloudCluster> listAllCloudClusters() {
        return cloudClusterRepository.findAllByTenantId(AuthUtils.getCurrentTenantId());
    }

    @Override
    public Optional<CloudCluster> getCloudCluster(String clusterId) {
        return cloudClusterRepository.findByTenantIdAndId(AuthUtils.getCurrentTenantId(), clusterId);
    }

    @Override
    public void deleteCloudCluster(String clusterId) {
        cloudClusterRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), clusterId);
    }
}
