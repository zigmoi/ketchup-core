package org.zigmoi.ketchup.tenantsetting.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zigmoi.ketchup.tenantsetting.dtos.*;
import org.zigmoi.ketchup.tenantsetting.entities.*;
import org.zigmoi.ketchup.tenantsetting.services.TenantSettingsService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
public class TenantSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(TenantSettingsController.class);

    @Autowired
    TenantSettingsService service;

    /* Git provider apis */
    @PostMapping("v1/tenant/settings/git-provider")
    public void createGitProvider(HttpServletRequest servletRequest, @RequestBody @Valid GitProviderSettingDto gitProviderSettingDto) {
        service.createGitProvider(gitProviderSettingDto);
    }

    @GetMapping("v1/tenant/settings/git-providers")
    public List<GitProviderSettingEntity> listAllGitProviders(HttpServletRequest servletRequest) {
        return service.listAllGitProviders();
    }

    @GetMapping("v1/tenant/settings/git-provider/{id}")
    public Optional<GitProviderSettingEntity> getGitProvider(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        return service.getGitProvider(id);
    }

    @DeleteMapping("v1/tenant/settings/git-provider/{id}")
    public void deleteGitProvider(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteGitProvider(id);
    }

    /* Build tool apis */
    @PostMapping("v1/tenant/settings/build-tool-file-upload")
    public void saveBuildToolSettings(HttpServletRequest servletRequest,
                                      @RequestParam("file[]") MultipartFile[] files,
                                      @RequestParam String provider,
                                      @RequestParam String settingName) {
        logger.info("Upload File Count - " + files.length);
        logger.info("settingName - " + settingName);
        logger.info("provider - " + provider);
        for (MultipartFile file: files){
            BuildToolSettingDto buildToolSettingDto = new BuildToolSettingDto();
            buildToolSettingDto.setProvider(provider);
            buildToolSettingDto.setDisplayName(settingName);
            buildToolSettingDto.setFileName(file.getName());
            service.createBuildTool(buildToolSettingDto, file);
        }
    }

    @GetMapping("v1/tenant/settings/build-tools")
    public List<BuildToolSettingEntity> listAllBuildTools(HttpServletRequest servletRequest) {
        return service.listAllBuildTools();
    }

    @GetMapping("v1/tenant/settings/build-tool/{id}")
    public Optional<BuildToolSettingEntity> getBuildTool(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        return service.getBuildTool(id);
    }

    @DeleteMapping("v1/tenant/settings/build-tool/{id}")
    public void deleteBuildTool(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteBuildTool(id);
    }

    /* Cloud credentials apis */
    @PostMapping("v1/tenant/settings/cloud-credential")
    public void saveCloudCredentialSetting(HttpServletRequest servletRequest, CloudProviderSettingDto cloudProviderSettingDto) {
        service.createCloudProvider(cloudProviderSettingDto);
    }

    @DeleteMapping("v1/tenant/settings/cloud-credential/{id}")
    public void deleteCloudCredential(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteCloudCredential(id);
    }

    /* Cloud registry apis */
    @PostMapping("v1/tenant/settings/cloud-registry")
    public void saveCloudRegistrySetting(HttpServletRequest servletRequest, CloudRegistrySettingDto cloudRegistrySettingDto) {
        service.createCloudRegistry(cloudRegistrySettingDto);
    }

    @GetMapping("v1/tenant/settings/cloud-registries")
    public List<ContainerRegistrySettingEntity> listAllCloudRegistries(HttpServletRequest servletRequest) {
        return service.listAllCloudRegistries();
    }

    @GetMapping("v1/tenant/settings/cloud-registry/{id}")
    public Optional<ContainerRegistrySettingEntity> getCloudRegistry(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        return service.getCloudRegistry(id);
    }

    @DeleteMapping("v1/tenant/settings/cloud-registry/{id}")
    public void deleteCloudRegistry(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteCloudRegistry(id);
    }

    /* Build tool apis */
    @PostMapping("v1/tenant/settings/cloud-cluster-file-upload")
    public void saveCloudClusterSettings(HttpServletRequest servletRequest,
                                         @RequestParam("file[]") MultipartFile[] files,
                                         @RequestParam String provider,
                                         @RequestParam String settingName) {
        logger.info("Upload File Count - " + files.length);
        logger.info("settingName - " + settingName);
        logger.info("provider - " + provider);
        for (MultipartFile file: files){
            CloudClusterSettingDto cloudClusterSettingDto = new CloudClusterSettingDto();
            cloudClusterSettingDto.setProvider(provider);
            cloudClusterSettingDto.setDisplayName(settingName);
            cloudClusterSettingDto.setFileName(file.getName());
            service.createCloudCluster(cloudClusterSettingDto, file);
        }
    }

    @GetMapping("v1/tenant/settings/cloud-clusters")
    public List<CloudProviderSettingEntity> listAllCloudClusters(HttpServletRequest servletRequest) {
        return service.listAllCloudClusters();
    }

    @GetMapping("v1/tenant/settings/cloud-cluster/{id}")
    public Optional<CloudProviderSettingEntity> getCloudCluster(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        return service.getCloudCluster(id);
    }

    @DeleteMapping("v1/tenant/settings/cloud-cluster/{id}")
    public void deleteCloudCluster(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteCloudCluster(id);
    }
}
