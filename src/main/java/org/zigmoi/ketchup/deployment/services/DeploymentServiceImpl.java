package org.zigmoi.ketchup.deployment.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zigmoi.ketchup.common.ConfigUtility;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.deployment.basicSpringBoot.BasicSpringBootDeploymentFlow;
import org.zigmoi.ketchup.deployment.basicSpringBoot.BasicSpringBootDeploymentFlowConstants;
import org.zigmoi.ketchup.deployment.dtos.*;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;
import org.zigmoi.ketchup.deployment.repositories.DeploymentRepository;
import org.zigmoi.ketchup.exception.UnexpectedException;
import org.zigmoi.ketchup.helm.services.HelmService;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.project.dtos.settings.BuildToolSettingsResponseDto;
import org.zigmoi.ketchup.project.dtos.settings.ContainerRegistrySettingsResponseDto;
import org.zigmoi.ketchup.project.dtos.settings.K8sHostAliasSettingsResponseDto;
import org.zigmoi.ketchup.project.dtos.settings.KubernetesClusterSettingsResponseDto;
import org.zigmoi.ketchup.project.services.ProjectSettingsService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
public class DeploymentServiceImpl implements DeploymentService {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    @Autowired
    private final DeploymentRepository deploymentRepository;

    @Autowired
    private ProjectSettingsService projectSettingsService;

    @Autowired
    private HelmService helmService;

    @Autowired
    public DeploymentServiceImpl(DeploymentRepository deploymentRepository) {
        this.deploymentRepository = deploymentRepository;
    }

    private String getNewDeploymentId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void updateDeploymentStatus(String projectResourceId, String deploymentResourceId, String status) {

    }

    @Override
    public void updateDeploymentDisplayName(String projectResourceId, String deploymentResourceId, String displayName) {

    }

    @Override
    @Transactional
    public String createDeployment(String projectResourceId, DeploymentRequestDto deploymentRequestDto) {
        DeploymentId deploymentId = new DeploymentId(AuthUtils.getCurrentTenantId(), projectResourceId, getNewDeploymentId());
        DeploymentEntity deploymentEntity = new DeploymentEntity();
        deploymentEntity.setId(deploymentId);
        deploymentEntity.setType(deploymentRequestDto.getApplicationType());
        deploymentEntity.setDisplayName(deploymentRequestDto.getDisplayName());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JSONObject deploymentJson = new JSONObject(objectMapper.writeValueAsString(deploymentRequestDto));
            JSONObject deploymentIdJson = new JSONObject(objectMapper.writeValueAsString(deploymentId));
            deploymentJson.put("deploymentId", deploymentIdJson);

            //get all setting values and store it in deployment.
            final KubernetesClusterSettingsResponseDto devKubernetesCluster = projectSettingsService.getKubernetesCluster(projectResourceId, deploymentRequestDto.getDevKubernetesClusterSettingId());
            final ContainerRegistrySettingsResponseDto containerRegistry = projectSettingsService.getContainerRegistry(projectResourceId, deploymentRequestDto.getContainerRegistrySettingId());
            final BuildToolSettingsResponseDto buildTool = projectSettingsService.getBuildTool(projectResourceId, deploymentRequestDto.getBuildToolSettingId());
            //save settings for host alias settings

            deploymentJson.put("devKubeconfig", devKubernetesCluster.getFileData());
            deploymentJson.put("containerRegistryType", containerRegistry.getType());
            deploymentJson.put("containerRegistryUrl", containerRegistry.getRegistryUrl());
            deploymentJson.put("containerRegistryUsername", containerRegistry.getRegistryUsername());
            deploymentJson.put("containerRegistryPassword", containerRegistry.getRegistryPassword());
            deploymentJson.put("containerRepositoryName", containerRegistry.getRepository());
            deploymentJson.put("buildToolType", buildTool.getType());
            deploymentJson.put("buildToolSettingsData", buildTool.getFileData());

            deploymentEntity.setData(deploymentJson.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        deploymentRepository.save(deploymentEntity);
        return deploymentId.getDeploymentResourceId();
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentDetailsDto getDeployment(String deploymentResourceId) {
        DeploymentEntity deploymentEntity = deploymentRepository.getByDeploymentResourceId(deploymentResourceId);
        ObjectMapper objectMapper = new ObjectMapper();
        DeploymentDetailsDto deploymentDetailsDto = null;
        try {
            deploymentDetailsDto = objectMapper.readValue(deploymentEntity.getData(), DeploymentDetailsDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deploymentDetailsDto;
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentResponseDto getDeploymentDetails(String deploymentResourceId) {
        DeploymentEntity deploymentEntity = deploymentRepository.getByDeploymentResourceId(deploymentResourceId);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DeploymentResponseDto deploymentResponseDto = null;
        try {
            deploymentResponseDto = objectMapper.readValue(deploymentEntity.getData(), DeploymentResponseDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deploymentResponseDto;
    }

    @Override
    public Optional<BasicSpringBootDeploymentResponseDto> getBasicSpringBootDeployment(String projectResourceId, String deploymentResourceId) {
        return Optional.empty();
    }

    @Override
    public void deleteDeployment(String projectResourceId, String deploymentResourceId) {
        //Considering cluster cannot be changed.
        DeploymentDetailsDto deploymentDetailsDto = getDeployment(deploymentResourceId);
        String namespace = deploymentDetailsDto.getDevKubernetesNamespace();
        String kubeConfig = StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig());
        helmService.uninstallChart("release-" + deploymentResourceId, namespace, kubeConfig);
    }

    @Override
    public List<DeploymentEntity> listAllBasicSpringBootDeployments(String projectResourceId) {
        return deploymentRepository.findAll()
                .stream()
                .filter(deploymentEntity ->
                        deploymentEntity.getId().getProjectResourceId().equalsIgnoreCase(projectResourceId))
                .collect(Collectors.toList());
    }

    @Override
    public void updateDeployment(String projectResourceId, String deploymentResourceId, DeploymentRequestDto deploymentRequestDto) {
        DeploymentEntity deployment = deploymentRepository.getByDeploymentResourceId(deploymentResourceId);
        DeploymentId deploymentId = deployment.getId();
        deployment.setDisplayName(deploymentRequestDto.getDisplayName());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JSONObject deploymentJson = new JSONObject(objectMapper.writeValueAsString(deploymentRequestDto));
            JSONObject deploymentIdJson = new JSONObject(objectMapper.writeValueAsString(deploymentId));
            deploymentJson.put("deploymentId", deploymentIdJson);

            //get all setting values and store it in deployment.
            final KubernetesClusterSettingsResponseDto devKubernetesCluster = projectSettingsService.getKubernetesCluster(projectResourceId, deploymentRequestDto.getDevKubernetesClusterSettingId());
            final ContainerRegistrySettingsResponseDto containerRegistry = projectSettingsService.getContainerRegistry(projectResourceId, deploymentRequestDto.getContainerRegistrySettingId());
            final BuildToolSettingsResponseDto buildTool = projectSettingsService.getBuildTool(projectResourceId, deploymentRequestDto.getBuildToolSettingId());
            //save settings for host alias settings

            deploymentJson.put("devKubeconfig", devKubernetesCluster.getFileData());
            deploymentJson.put("containerRegistryType", containerRegistry.getType());
            deploymentJson.put("containerRegistryUrl", containerRegistry.getRegistryUrl());
            deploymentJson.put("containerRegistryUsername", containerRegistry.getRegistryUsername());
            deploymentJson.put("containerRegistryPassword", containerRegistry.getRegistryPassword());
            deploymentJson.put("containerRepositoryName", containerRegistry.getRepository());
            deploymentJson.put("buildToolType", buildTool.getType());
            deploymentJson.put("buildToolSettingsData", buildTool.getFileData());

            deployment.setData(deploymentJson.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        deploymentRepository.save(deployment);
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

//        entity.setType(DeploymentsType.BASIC_SPRING_BOOT.toString());
//        entity.setServiceName(dto.getServiceName());
//        entity.setCurrentStatus(DeploymentsStatus.INITIALISED.toString());

//        entity.setData(); todo snappy compress

        deploymentRepository.saveAndFlush(entity);

        // todo if deployment already running delete it
        new Thread(flow::execute).start();

        return config.toString();
    }

    private JSONObject convertToFlowConfig(String projectResourceId, String deploymentResourceId, BasicSpringBootDeploymentRequestDto dto) {

        JSONObject config = new JSONObject();
        config.put("tenant-id", AuthUtils.getCurrentTenantId());
        config.put("project-id", projectResourceId);
        config.put("deployment-id", deploymentResourceId);
//        config.put("deployment-flow-type", DeploymentsType.BASIC_SPRING_BOOT.toString());
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
        K8sHostAliasSettingsResponseDto hostnameIpMappingSettingsResponseDto = isNullOrEmpty(dto.getExternalResourceIpHostnameMappingSettingId()) ? null :
                projectSettingsService.getK8sHostAlias(projectResourceId, dto.getExternalResourceIpHostnameMappingSettingId());

        JSONArray argsJa = new JSONArray();
        JSONObject argsJ = new JSONObject();

        String dockerRegistryVendor = stageBuildSpringBootDockerImageArgs.getJSONObject(0).getString("docker-registry-vendor");

        File kubeconfig = File.createTempFile("zigmoi-ketchup-tmp-", ".yaml");
        FileUtility.createAndWrite(kubeconfig, StringUtility.decodeBase64(kubernetesClusterSettingsResponseDto.getFileData()));
        argsJ.put("kubeconfig-file-path", kubeconfig.getAbsolutePath());
        argsJ.put("namespace", dto.getKubernetesNamespace());
        argsJ.put("app-id", dto.getServiceName());
        argsJ.put("patch-deployment-if-exists", String.valueOf(dto.isUpdateDeploymentIfRunning()));
//        if (!CloudProviders.AWS.toString().equals(kubernetesClusterSettingsResponseDto.getProvider())
//                && ContainerRegistryProviders.AWS_ECR.toString().equals(dockerRegistryVendor)) {
//            throw new UnsupportedOperationException("When using " + ContainerRegistryProviders.AWS_ECR
//                    + ", Cloud Provider must be : " + CloudProviders.AWS);
//        }
//        argsJ.put("vm-vendor", kubernetesClusterSettingsResponseDto.getProvider());
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
//        CloudProviderSettingsResponseDto cloudProvider = projectSettingsService.getCloudProvider(projectResourceId, dto.getCloudProviderSettingId());

        JSONArray argsJa = new JSONArray();

        String basePath = ((JSONObject) stageMvnCleanInstallArgs.get(0)).getString("base-path")
                + "/" + ((JSONObject) stageMvnCleanInstallArgs.get(0)).getString("repo-name");
        String dockerFilePath = basePath + "/" + "Dockerfile";
        String dockerRegistryVendor = containerRegistry.getType();
        if (!ContainerRegistryProviders.AWS_ECR.toString().equals(dockerRegistryVendor)) {
            throw new UnsupportedOperationException("Container provider not supported yet");
        }

        JSONObject dockerRegistryVendorArgsJ = new JSONObject();
        dockerRegistryVendorArgsJ.put("repo", dto.getDockerImageRepoName());
        dockerRegistryVendorArgsJ.put("registry-id", containerRegistry.getRegistryUsername());
        dockerRegistryVendorArgsJ.put("registry-base-url", containerRegistry.getRegistryUrl());
//        dockerRegistryVendorArgsJ.put("aws-access-key-id", cloudProvider.getAccessId());
//        dockerRegistryVendorArgsJ.put("aws-secret-key", cloudProvider.getSecretKey());

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

//        GitProviderSettingsResponseDto gitProvider = projectSettingsService.getGitProvider(projectResourceId, dto.getGitProviderSettingId());

        JSONArray argsJa = new JSONArray();

        JSONObject argsJ = new JSONObject();
        argsJ.put("base-path", ConfigUtility.instance().getProperty("deployment.basic-spring-boot.pull-from-remote.tmp-build-base-path"));
        argsJ.put("repo-name", dto.getGitRepoName());
//        argsJ.put("git-vendor", gitProvider.getProvider());
        JSONObject gitVendorArgJ = new JSONObject();
//        gitVendorArgJ.put("url", constructRepoUrl(gitProvider.getProvider(), dto.getGitRepoName()));
//        gitVendorArgJ.put("username", gitProvider.getUsername());
//        gitVendorArgJ.put("password", gitProvider.getPassword());
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

}
