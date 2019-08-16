package org.zigmoi.ketchup.iam.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zigmoi.ketchup.iam.dtos.*;
import org.zigmoi.ketchup.iam.entities.*;
import org.zigmoi.ketchup.iam.services.TenantResourceService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
public class TenantResourceController {
    private static final Logger logger = Logger.getLogger(TenantResourceController.class.toString());

    @Autowired
    TenantResourceService service;

    /* Git provider apis */
    @PostMapping("/v1/git-provider")
    public void createGitProvider(HttpServletRequest servletRequest, @RequestBody @Valid GitProviderDto gitProviderDto) {
        service.createGitProvider(gitProviderDto);
    }

    @GetMapping("/v1/git-providers")
    public List<GitProvider> listAllGitProviders(HttpServletRequest servletRequest) {
        return service.listAllGitProviders();
    }

    @GetMapping("/v1/git-provider/{id}")
    public Optional<GitProvider> getGitProvider(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        return service.getGitProvider(id);
    }

    @DeleteMapping("/v1/git-provider/{id}")
    public void deleteGitProvider(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteGitProvider(id);
    }

    /* Build tool apis */
    @PostMapping("/v1/build-tool-file-upload")
    public void saveBuildToolSettings(HttpServletRequest servletRequest,
                                      @RequestParam("file[]") MultipartFile[] files,
                                      @RequestParam String provider,
                                      @RequestParam String settingName) {
        logger.info("Upload File Count - " + files.length);
        logger.info("settingName - " + settingName);
        logger.info("provider - " + provider);
        for (MultipartFile file: files){
            BuildToolDto buildToolDto = new BuildToolDto();
            buildToolDto.setProvider(provider);
            buildToolDto.setDisplayName(settingName);
            buildToolDto.setFileName(file.getName());
            service.createBuildTool(buildToolDto, file);
        }
    }

    @GetMapping("/v1/build-tools")
    public List<BuildTool> listAllBuildTools(HttpServletRequest servletRequest) {
        return service.listAllBuildTools();
    }

    @GetMapping("/v1/build-tool/{id}")
    public Optional<BuildTool> getBuildTool(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        return service.getBuildTool(id);
    }

    @DeleteMapping("/v1/build-tool/{id}")
    public void deleteBuildTool(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteBuildTool(id);
    }

    /* Cloud credentials apis */
    @PostMapping("/v1/cloud-credential")
    public void saveCloudCredentialSetting(HttpServletRequest servletRequest, CloudCredentialDto cloudCredentialDto) {
        service.createCloudCredential(cloudCredentialDto);
    }

    @GetMapping("/v1/cloud-credentials")
    public List<CloudCredential> listAllCloudCredentials(HttpServletRequest servletRequest) {
        return service.listAllCloudCredentials();
    }

    @GetMapping("/v1/cloud-credential/{id}")
    public Optional<CloudCredential> getCloudCredential(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        return service.getCloudCredential(id);
    }

    @DeleteMapping("/v1/cloud-credential/{id}")
    public void deleteCloudCredential(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteCloudCredential(id);
    }

    /* Cloud registry apis */
    @PostMapping("/v1/cloud-registry")
    public void saveCloudRegistrySetting(HttpServletRequest servletRequest, CloudRegistryDto cloudRegistryDto) {
        service.createCloudRegistry(cloudRegistryDto);
    }

    @GetMapping("/v1/cloud-registries")
    public List<CloudRegistry> listAllCloudRegistries(HttpServletRequest servletRequest) {
        return service.listAllCloudRegistries();
    }

    @GetMapping("/v1/cloud-registry/{id}")
    public Optional<CloudRegistry> getCloudRegistry(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        return service.getCloudRegistry(id);
    }

    @DeleteMapping("/v1/cloud-registry/{id}")
    public void deleteCloudRegistry(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteCloudRegistry(id);
    }

    /* Build tool apis */
    @PostMapping("/v1/cloud-cluster-file-upload")
    public void saveCloudClusterSettings(HttpServletRequest servletRequest,
                                         @RequestParam("file[]") MultipartFile[] files,
                                         @RequestParam String provider,
                                         @RequestParam String settingName) {
        logger.info("Upload File Count - " + files.length);
        logger.info("settingName - " + settingName);
        logger.info("provider - " + provider);
        for (MultipartFile file: files){
            CloudClusterDto cloudClusterDto = new CloudClusterDto();
            cloudClusterDto.setProvider(provider);
            cloudClusterDto.setDisplayName(settingName);
            cloudClusterDto.setFileName(file.getName());
            service.createCloudCluster(cloudClusterDto, file);
        }
    }

    @GetMapping("/v1/cloud-clusters")
    public List<CloudCluster> listAllCloudClusters(HttpServletRequest servletRequest) {
        return service.listAllCloudClusters();
    }

    @GetMapping("/v1/cloud-cluster/{id}")
    public Optional<CloudCluster> getCloudCluster(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        return service.getCloudCluster(id);
    }

    @DeleteMapping("/v1/cloud-cluster/{id}")
    public void deleteCloudCluster(HttpServletRequest servletRequest, @PathVariable("id") String id) {
        service.deleteCloudCluster(id);
    }
}
