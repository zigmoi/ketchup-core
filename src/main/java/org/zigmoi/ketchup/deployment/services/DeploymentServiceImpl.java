package org.zigmoi.ketchup.deployment.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zigmoi.ketchup.common.ConfigUtility;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.deployment.basicSpringBoot.BasicSpringBootDeploymentFlow;
import org.zigmoi.ketchup.deployment.basicSpringBoot.BasicSpringBootDeploymentFlowConstants;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentRequestDto;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentResponseDto;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;
import org.zigmoi.ketchup.deployment.repositories.DeploymentAclRepository;
import org.zigmoi.ketchup.deployment.repositories.DeploymentRepository;
import org.zigmoi.ketchup.exception.UnexpectedException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.UserService;
import org.zigmoi.ketchup.project.dtos.settings.*;
import org.zigmoi.ketchup.project.services.ProjectService;
import org.zigmoi.ketchup.project.services.ProjectSettingsService;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
public class DeploymentServiceImpl implements DeploymentService {

    private final DeploymentAclRepository deploymentAclRepository;
    private final DeploymentRepository deploymentRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectSettingsService projectSettingsService;

    @Autowired
    public DeploymentServiceImpl(DeploymentAclRepository deploymentAclRepository, DeploymentRepository deploymentRepository) {
        this.deploymentAclRepository = deploymentAclRepository;
        this.deploymentRepository = deploymentRepository;
    }

    @Override
    public void updateDeploymentStatus(String projectResourceId, String deploymentResourceId, String status) {

    }

    @Override
    public void updateDeploymentDisplayName(String projectResourceId, String deploymentResourceId, String displayName) {

    }

    @Override
    public String createBasicSpringBootDeployment(String projectResourceId, BasicSpringBootDeploymentRequestDto dto) {

        String id = getNewDeploymentId();

        JSONObject config = convertToFlowConfig(projectResourceId, id, dto);
        BasicSpringBootDeploymentFlow flow = new BasicSpringBootDeploymentFlow(config);
        flow.validate();

        DeploymentId deploymentId = new DeploymentId(AuthUtils.getCurrentTenantId(), projectResourceId, flow.getId());
        DeploymentEntity entity = new DeploymentEntity();
        entity.setId(deploymentId);

        entity.setType(DeploymentsType.BASIC_SPRING_BOOT.toString());
        entity.setServiceName(dto.getServiceName());
        entity.setCurrentStatus(DeploymentsStatus.INITIALISED.toString());

        entity.setCreatedBy(AuthUtils.getCurrentUsername());
        entity.setCreatedOn(new Date());
        entity.setDisplayName(dto.getDisplayName());
        entity.setLastUpdatedBy(AuthUtils.getCurrentUsername());
        entity.setLastUpdatedOn(new Date());
//        entity.setData(); todo snappy compress

        deploymentRepository.saveAndFlush(entity);

        // todo if deployment already running delete it
        new Thread(flow::execute).start();

        return config.toString();
    }

    private String getNewDeploymentId() {
        return UUID.randomUUID().toString();
    }

    private JSONObject convertToFlowConfig(String projectResourceId, String deploymentResourceId, BasicSpringBootDeploymentRequestDto dto) {

        JSONObject config = new JSONObject();
        config.put("tenant-id", AuthUtils.getCurrentTenantId());
        config.put("project-id", projectResourceId);
        config.put("deployment-id", deploymentResourceId);
        config.put("deployment-flow-type", DeploymentsType.BASIC_SPRING_BOOT.toString());
        config.put("config-template-version", "v1");

        JSONArray stagesJA = new JSONArray();
        JSONObject stagePullFromRemoteJ;
        try {
            stagePullFromRemoteJ = getPullFromRemoteConfig(projectResourceId, dto);
        } catch (Exception e) {
            throw new UnexpectedException("Failed to generate stagePullFromRemote config", e);
        }
        JSONObject stageMvnCleanInstallJ;
        try {
            stageMvnCleanInstallJ = getMvnCleanInstallConfig(projectResourceId,
                    stagePullFromRemoteJ.getJSONArray("args"), dto);
        } catch (Exception e) {
            throw new UnexpectedException("Failed to generate stageMvnCleanInstall config", e);
        }
        JSONObject stageBuildSpringBootDockerImageJ;
        try {
            stageBuildSpringBootDockerImageJ = getBuildSpringBootDockerImageConfig(projectResourceId,
                    stageMvnCleanInstallJ.getJSONArray("args"), dto);
        } catch (Exception e) {
            throw new UnexpectedException("Failed to generate stageBuildSpringBootDockerImage config", e);
        }
        JSONObject stageDeployInKubernetesJ;
        try {
            stageDeployInKubernetesJ = getDeployInKubernetesConfig(projectResourceId,
                    stageBuildSpringBootDockerImageJ.getJSONArray("args"), dto);
        } catch (Exception e) {
            throw new UnexpectedException("Failed to generate stageBuildSpringBootDockerImage config", e);
        }

        stagesJA.put(stagePullFromRemoteJ);
        stagesJA.put(stageMvnCleanInstallJ);
        stagesJA.put(stageBuildSpringBootDockerImageJ);
        stagesJA.put(stageDeployInKubernetesJ);

        config.put("stages", stagesJA);
        return config;
    }

    private JSONObject getDeployInKubernetesConfig(String projectResourceId, JSONArray stageBuildSpringBootDockerImageArgs,
                                                   BasicSpringBootDeploymentRequestDto dto) throws IOException {
        JSONObject config = new JSONObject();
        config.put("command", BasicSpringBootDeploymentFlowConstants.C_DEPLOY_IN_KUBERNETES);
        config.put("arg-schema", "v1");

        KubernetesClusterSettingsResponseDto kubernetesClusterSettingsResponseDto =
                projectSettingsService.getKubernetesCluster(projectResourceId, dto.getKubernetesClusterSettingId());
        HostnameIpMappingSettingsResponseDto hostnameIpMappingSettingsResponseDto = isNullOrEmpty(dto.getExternalResourceIpHostnameMappingSettingId()) ? null :
                projectSettingsService.getHostnameIpMapping(projectResourceId, dto.getExternalResourceIpHostnameMappingSettingId());

        JSONArray argsJa = new JSONArray();
        JSONObject argsJ = new JSONObject();

        String dockerRegistryVendor = stageBuildSpringBootDockerImageArgs.getJSONObject(0).getString("docker-registry-vendor");

        File kubeconfig = File.createTempFile("zigmoi-ketchup-tmp-", ".yaml");
        FileUtility.createAndWrite(kubeconfig, StringUtility.decodeBase64(kubernetesClusterSettingsResponseDto.getFileData()));
        argsJ.put("kubeconfig-file-path", kubeconfig.getAbsolutePath());
        argsJ.put("namespace", dto.getKubernetesNamespace());
        argsJ.put("app-id", dto.getServiceName());
        argsJ.put("patch-deployment-if-exists", String.valueOf(dto.isUpdateDeploymentIfRunning()));
        if (!CloudProviders.AWS.toString().equals(kubernetesClusterSettingsResponseDto.getProvider())
                && ContainerRegistryProviders.AWS_ECR.toString().equals(dockerRegistryVendor)) {
            throw new UnsupportedOperationException("When using " + ContainerRegistryProviders.AWS_ECR
                    + ", Cloud Provider must be : " + CloudProviders.AWS);
        }
        argsJ.put("vm-vendor", kubernetesClusterSettingsResponseDto.getProvider());
        argsJ.put("docker-registry-vendor", stageBuildSpringBootDockerImageArgs.getJSONObject(0).getString("docker-registry-vendor"));
        argsJ.put("docker-registry-vendor-args",
                stageBuildSpringBootDockerImageArgs.getJSONObject(0).getJSONObject("docker-registry-vendor-args"));
        argsJ.put("docker-build-image-name", stageBuildSpringBootDockerImageArgs.getJSONObject(0).getString("docker-build-image-name"));
        argsJ.put("docker-build-image-tag", stageBuildSpringBootDockerImageArgs.getJSONObject(0).getString("docker-build-image-tag"));
        argsJ.put("port", dto.getAppServerPort());
        if (hostnameIpMappingSettingsResponseDto != null) {
            Map<String, Set<String>> ipHostNames = new HashMap<>();
            for (Map.Entry<String, String> entry : hostnameIpMappingSettingsResponseDto.getHostnameIpMapping().entrySet()) {
                Set<String> hostNames = ipHostNames.computeIfAbsent(entry.getValue(), i -> new HashSet<>());
                hostNames.add(entry.getKey());
            }
        }

        argsJa.put(argsJ);

        config.put("args", argsJa);
        return config;
    }

    private JSONObject getBuildSpringBootDockerImageConfig(String projectResourceId, JSONArray stageMvnCleanInstallArgs,
                                                           BasicSpringBootDeploymentRequestDto dto) {
        JSONObject config = new JSONObject();
        config.put("command", BasicSpringBootDeploymentFlowConstants.C_BUILD_SPRING_BOOT_DOCKER_IMAGE);
        config.put("arg-schema", "v1");

        ContainerRegistrySettingsResponseDto containerRegistry = projectSettingsService.getContainerRegistry(projectResourceId, dto.getContainerRegistrySettingId());
        CloudProviderSettingsResponseDto cloudProvider = projectSettingsService.getCloudProvider(projectResourceId, dto.getCloudProviderSettingId());

        JSONArray argsJa = new JSONArray();

        String basePath = ((JSONObject) stageMvnCleanInstallArgs.get(0)).getString("base-path")
                + "/" + ((JSONObject) stageMvnCleanInstallArgs.get(0)).getString("repo-name");
        String dockerFilePath = basePath + "/" + "Dockerfile";
        String dockerRegistryVendor = containerRegistry.getProvider();
        if (!ContainerRegistryProviders.AWS_ECR.toString().equals(dockerRegistryVendor)) {
            throw new UnsupportedOperationException("Container provider not supported yet");
        }

        JSONObject dockerRegistryVendorArgsJ = new JSONObject();
        dockerRegistryVendorArgsJ.put("repo", dto.getDockerImageRepoName());
        dockerRegistryVendorArgsJ.put("registry-id", containerRegistry.getRegistryId());
        dockerRegistryVendorArgsJ.put("registry-base-url", containerRegistry.getRegistryUrl());
        dockerRegistryVendorArgsJ.put("aws-access-key-id", cloudProvider.getAccessId());
        dockerRegistryVendorArgsJ.put("aws-secret-key", cloudProvider.getSecretKey());

        JSONObject dockerFileTemplateArgsJ = new JSONObject();
        dockerFileTemplateArgsJ.put("app-home", dto.getAppBasePath());
        dockerFileTemplateArgsJ.put("timezone", dto.getAppTimezone());
        dockerFileTemplateArgsJ.put("port", dto.getAppServerPort());

        JSONObject argsJ = new JSONObject();

        argsJ.put("base-path", basePath);
        argsJ.put("docker-file-path", dockerFilePath);
        argsJ.put("docker-registry-vendor", dockerRegistryVendor);
        argsJ.put("docker-registry-vendor-args", dockerRegistryVendorArgsJ);
        argsJ.put("docker-build-image-name", dto.getDockerImageName());
        argsJ.put("docker-build-image-tag", dto.getDockerImageTag());
        argsJ.put("docker-file-template-path", ConfigUtility.instance().getProperty("deployment.basic-spring-boot.build-spring-boot-docker-image.template-file"));
        argsJ.put("docker-file-template-args", dockerFileTemplateArgsJ);

        argsJa.put(argsJ);

        config.put("args", argsJa);
        return config;
    }

    private JSONObject getMvnCleanInstallConfig(String projectResourceId, JSONArray stagePullFromRemoteArgs,
                                                BasicSpringBootDeploymentRequestDto dto) throws IOException {
        JSONObject config = new JSONObject();
        config.put("command", BasicSpringBootDeploymentFlowConstants.C_MAVEN_CLEAN_INSTALL);
        config.put("arg-schema", "v1");

        BuildToolSettingsResponseDto settings = projectSettingsService.getBuildTool(projectResourceId, dto.getBuildToolSettingId());

        JSONArray argsJa = new JSONArray();

        String basePath = ((JSONObject) stagePullFromRemoteArgs.get(0)).getString("base-path");

        JSONObject argsJ = new JSONObject();
        argsJ.put("base-path", basePath);
        argsJ.put("repo-name", dto.getGitRepoName());
        argsJ.put("build-path", basePath + "/" + dto.getGitRepoName());
        argsJ.put("maven-command-path",
                ConfigUtility.instance().getProperty("deployment.basic-spring-boot.maven-clean-install.maven-command-path"));
        argsJ.put("branch-name", dto.getGitRepoBranchName());
        argsJ.put("commit-id", dto.getGitRepoCommitId());
        File mvnPrivateSettings = File.createTempFile("zigmoi-ketchup-tmp-", ".xml");
        FileUtility.createAndWrite(mvnPrivateSettings, StringUtility.decodeBase64(settings.getFileData()));
        argsJ.put("maven-private-repo-settings-path", mvnPrivateSettings.getAbsolutePath());

        argsJa.put(argsJ);

        config.put("args", argsJa);
        return config;
    }

    private JSONObject getPullFromRemoteConfig(String projectResourceId, BasicSpringBootDeploymentRequestDto dto) {
        JSONObject config = new JSONObject();
        config.put("command", BasicSpringBootDeploymentFlowConstants.C_PULL_FROM_REMOTE);
        config.put("arg-schema", "v1");

        GitProviderSettingsResponseDto gitProvider = projectSettingsService.getGitProvider(projectResourceId, dto.getGitProviderSettingId());

        JSONArray argsJa = new JSONArray();

        JSONObject argsJ = new JSONObject();
        argsJ.put("base-path", ConfigUtility.instance().getProperty("deployment.basic-spring-boot.pull-from-remote.tmp-build-base-path"));
        argsJ.put("repo-name", dto.getGitRepoName());
        argsJ.put("git-vendor", gitProvider.getProvider());
        JSONObject gitVendorArgJ = new JSONObject();
        gitVendorArgJ.put("url", constructRepoUrl(gitProvider.getProvider(), dto.getGitRepoName()));
        gitVendorArgJ.put("username", gitProvider.getUsername());
        gitVendorArgJ.put("password", gitProvider.getPassword());
        argsJ.put("git-vendor-arg", gitVendorArgJ);

        argsJa.put(argsJ);

        config.put("args", argsJa);
        return config;
    }

    private String constructRepoUrl(String provider, String repoName) {
        String url;
        repoName = repoName.startsWith("/") ? repoName.substring(1) : repoName;
        if (GitProviders.BITBUCKET.toString().equals(provider)) {
            url = "https://bitbucket.org/" + repoName;
        } else if (GitProviders.GITLAB.toString().equals(provider)) {
            url = "https://gitlab.com/" + repoName;
        } else {
            throw new UnsupportedOperationException("Not supported for provider : " + provider);
        }
        return url;
    }

    @Override
    public Optional<BasicSpringBootDeploymentResponseDto> getBasicSpringBootDeployment(String projectResourceId, String deploymentResourceId) {
        return Optional.empty();
    }

    @Override
    public void deleteDeployment(String projectResourceId, String deploymentResourceId) {

    }

    @Override
    public List<BasicSpringBootDeploymentResponseDto> listAllBasicSpringBootDeployments(String projectResourceId) {
        return null;
    }
}
