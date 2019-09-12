package org.zigmoi.ketchup.tenantsetting.services;

import net.openhft.hashing.LongHashFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.tenantsetting.dtos.*;
import org.zigmoi.ketchup.tenantsetting.entities.*;
import org.zigmoi.ketchup.tenantsetting.repositories.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TenantSettingsServiceImpl implements TenantSettingsService {

    @Autowired
    private GitProviderSettingRepository gitProviderSettingRepository;

    @Autowired
    private BuildToolSettingRepository buildToolSettingRepository;

    @Autowired
    private CloudProviderSettingRepository cloudProviderSettingRepository;

    @Autowired
    private ContainerRegistrySettingRepository containerRegistrySettingRepository;

    @Autowired
    private KubernetesClusterSettingRepository kubernetesClusterSettingRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /* Git provider services */
    @Override
    public void createGitProvider(@Valid GitProviderSettingDto dto) {
        String uid = dto.getProvider() + "|" + dto.getDisplayName() + "|" + dto.getUsername()
                + "|" + dto.getRepoListUrl() + "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        GitProviderSettingEntity setting = new GitProviderSettingEntity();
        setting.setId(id);
        setting.setTenantId(AuthUtils.getCurrentTenantId());
        setting.setProvider(dto.getProvider());
        setting.setRepoListUrl(dto.getRepoListUrl());
        setting.setDisplayName(dto.getDisplayName());
        setting.setUsername(dto.getUsername());
        setting.setPassword(passwordEncoder.encode(dto.getPassword()));
        setting.setCreatedBy(AuthUtils.getCurrentUsername());
        setting.setCreatedOn(new Date());
        gitProviderSettingRepository.save(setting);
    }

    @Override
    public List<GitProviderSettingEntity> listAllGitProviders() {
        return gitProviderSettingRepository.findAllByTenantId(AuthUtils.getCurrentTenantId());
    }

    @Override
    public Optional<GitProviderSettingEntity> getGitProvider(String gitProviderId) {
        return gitProviderSettingRepository.findByTenantIdAndId(AuthUtils.getCurrentTenantId(), gitProviderId);
    }

    @Override
    public void deleteGitProvider(String gitProviderId) {
        gitProviderSettingRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), gitProviderId);
    }

    /* Build tool services */
    @Override
    public void createBuildTool(BuildToolSettingDto buildToolSettingDto, MultipartFile file) {
        String fileRemoteUrl = saveBuildToolFile(file);
        buildToolSettingDto.setFileRemoteUrl(fileRemoteUrl);

        String uid = file.getName() + "|" + buildToolSettingDto.getDisplayName() + "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        BuildToolSettingEntity setting = new BuildToolSettingEntity();
        setting.setId(id);
        setting.setTenantId(AuthUtils.getCurrentTenantId());
        setting.setProvider(buildToolSettingDto.getProvider());
        setting.setDisplayName(buildToolSettingDto.getDisplayName());
        setting.setFileName(buildToolSettingDto.getFileName());
        setting.setFileRemoteUrl(buildToolSettingDto.getFileRemoteUrl());
        setting.setCreatedBy(AuthUtils.getCurrentUsername());
        setting.setCreatedOn(new Date());
        buildToolSettingRepository.save(setting);
    }

    private String saveBuildToolFile(MultipartFile file) { // TODO: 16/8/19 implement
        return file.getName();
    }

    @Override
    public List<BuildToolSettingEntity> listAllBuildTools() {
        return buildToolSettingRepository.findAllByTenantId(AuthUtils.getCurrentTenantId());
    }

    @Override
    public Optional<BuildToolSettingEntity> getBuildTool(String buildToolId) {
        return buildToolSettingRepository.findByTenantIdAndId(AuthUtils.getCurrentTenantId(), buildToolId);
    }

    @Override
    public void deleteBuildTool(String buildToolId) {
        buildToolSettingRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), buildToolId);
    }

    /* Cloud credentials services */
    @Override
    public void createCloudProvider(CloudProviderSettingDto dto) {
        String uid = dto.getProvider() + "|" + dto.getDisplayName() + "|" + dto.getAccessId()
                + "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        CloudProviderSettingEntity setting = new CloudProviderSettingEntity();
        setting.setId(id);
        setting.setTenantId(AuthUtils.getCurrentTenantId());
        setting.setProvider(dto.getProvider());
        setting.setDisplayName(dto.getDisplayName());
        setting.setAccessId(dto.getAccessId());
        setting.setSecretKey(dto.getSecretKey());
        setting.setCreatedBy(AuthUtils.getCurrentUsername());
        setting.setCreatedOn(new Date());
        cloudProviderSettingRepository.save(setting);
    }

    @Override
    public void deleteCloudCredential(String credentialId) {
        cloudProviderSettingRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), credentialId);
    }

    /* Cloud registry services */
    @Override
    public void createCloudRegistry(CloudRegistrySettingDto dto) {
        String uid = dto.getProvider() + "|" + dto.getDisplayName() + "|" + dto.getRegistryId()
                + "|" + dto.getRegistryUrl() + "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        ContainerRegistrySettingEntity setting = new ContainerRegistrySettingEntity();
        setting.setId(id);
        setting.setTenantId(AuthUtils.getCurrentTenantId());
        setting.setProvider(dto.getProvider());
        setting.setDisplayName(dto.getDisplayName());
        setting.setCloudCredentialId(dto.getCloudCredentialId());
        setting.setRegistryId(dto.getRegistryId());
        setting.setRegistryUrl(dto.getRegistryUrl());
        setting.setCreatedBy(AuthUtils.getCurrentUsername());
        setting.setCreatedOn(new Date());
        containerRegistrySettingRepository.save(setting);
    }

    @Override
    public List<ContainerRegistrySettingEntity> listAllCloudRegistries() {
        return containerRegistrySettingRepository.findAllByTenantId(AuthUtils.getCurrentTenantId());
    }

    @Override
    public Optional<ContainerRegistrySettingEntity> getCloudRegistry(String registryId) {
        return containerRegistrySettingRepository.findByTenantIdAndId(AuthUtils.getCurrentTenantId(), registryId);
    }

    @Override
    public void deleteCloudRegistry(String registryId) {
        containerRegistrySettingRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), registryId);
    }

    /* cloud cluster services */
    @Override
    public void createCloudCluster(CloudClusterSettingDto dto, MultipartFile file) {
        String fileRemoteUrl = saveCloudClusterFile(file);
        String uid = dto.getProvider() + "|" + dto.getDisplayName() + "|" + dto.getFileName()
                + "|" + fileRemoteUrl+ "|" + AuthUtils.getCurrentTenantId()
                + "|" + AuthUtils.getCurrentUsername();
        String id = String.valueOf(LongHashFunction.xx_r39().hashChars(uid));

        CloudProviderSettingEntity setting = new CloudProviderSettingEntity();
        setting.setId(id);
        setting.setTenantId(AuthUtils.getCurrentTenantId());
        setting.setProvider(dto.getProvider());
        setting.setDisplayName(dto.getDisplayName());
        setting.setFileName(dto.getFileName());
        setting.setFileRemoteUrl(dto.getFileRemoteUrl());
        setting.setCreatedBy(AuthUtils.getCurrentUsername());
        setting.setCreatedOn(new Date());
        kubernetesClusterSettingRepository.save(setting);
    }

    private String saveCloudClusterFile(MultipartFile file) {
        return file.getName();
    }

    @Override
    public List<CloudProviderSettingEntity> listAllCloudClusters() {
        return kubernetesClusterSettingRepository.findAllByTenantId(AuthUtils.getCurrentTenantId());
    }

    @Override
    public Optional<CloudProviderSettingEntity> getCloudCluster(String clusterId) {
        return kubernetesClusterSettingRepository.findByTenantIdAndId(AuthUtils.getCurrentTenantId(), clusterId);
    }

    @Override
    public void deleteCloudCluster(String clusterId) {
        kubernetesClusterSettingRepository.deleteByTenantIdAndId(AuthUtils.getCurrentTenantId(), clusterId);
    }
}
