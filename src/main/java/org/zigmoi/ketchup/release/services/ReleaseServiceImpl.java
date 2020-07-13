package org.zigmoi.ketchup.release.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.deployment.dtos.DeploymentDetailsDto;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;
import org.zigmoi.ketchup.deployment.services.DeploymentService;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;
import org.zigmoi.ketchup.release.entities.PipelineResource;
import org.zigmoi.ketchup.release.entities.PipelineResourceId;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;
import org.zigmoi.ketchup.release.repositories.PipelineResourceRepository;
import org.zigmoi.ketchup.release.repositories.ReleaseRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ReleaseServiceImpl extends TenantProviderService implements ReleaseService {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseServiceImpl.class);

    private final ReleaseRepository releaseRepository;

    private final PipelineResourceRepository pipelineResourceRepository;

    private PermissionUtilsService permissionUtilsService;

    private DeploymentService deploymentService;

    ResourceLoader resourceLoader;

    @Autowired
    public ReleaseServiceImpl(ReleaseRepository releaseRepository, PipelineResourceRepository pipelineResourceRepository, PermissionUtilsService permissionUtilsService, DeploymentService deploymentService, ResourceLoader resourceLoader) {
        this.releaseRepository = releaseRepository;
        this.pipelineResourceRepository = pipelineResourceRepository;
        this.permissionUtilsService = permissionUtilsService;
        this.deploymentService = deploymentService;
        this.resourceLoader = resourceLoader;
    }


    @Override
    @Transactional
    public void create(String deploymentResourceId) {
        //validate and fetch deploymentId.
        //get projectId from deployment.
        //handle duplicate pipeline resources error in k8s.
        DeploymentDetailsDto deploymentDetailsDto = deploymentService.getDeployment(deploymentResourceId);

        String tenantId = AuthUtils.getCurrentTenantId();
        String projectResourceId = deploymentDetailsDto.getDeploymentId().getProjectResourceId();

        Release r = new Release();
        ReleaseId releaseId = new ReleaseId();
        String releaseResourceId = UUID.randomUUID().toString();
        releaseId.setReleaseResourceId(releaseResourceId);
        releaseId.setTenantId(tenantId);
        r.setId(releaseId);
        r.setDeploymentResourceId(deploymentResourceId);
        r.setProjectResourceId(projectResourceId);
        r.setHelmReleaseId(getHelmReleaseId(deploymentResourceId));

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            r.setDeploymentDataJson(objectMapper.writeValueAsString(deploymentDetailsDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        //generate pipeline all resources and save them, name can be parsed from them directly.
        List<PipelineResource> pipelineResources = generatePipelineResources("sb-standard-dev-1.0",
                deploymentDetailsDto, releaseResourceId);
        pipelineResources.forEach(pipelineResource ->
        {
            PipelineResourceId pipelineResourceId = new PipelineResourceId();
            pipelineResourceId.setTenantId(tenantId);
            pipelineResourceId.setGuid(UUID.randomUUID().toString());
            pipelineResource.setId(pipelineResourceId);
            pipelineResource.setProjectResourceId(projectResourceId);
            pipelineResource.setReleaseResourceId(releaseResourceId);
        });
        pipelineResourceRepository.saveAll(pipelineResources);
        releaseRepository.save(r);

        String kubeConfig = StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig());
        deployPipelineResources(pipelineResources, kubeConfig);
    }

    private String getHelmReleaseId(String deploymentResourceId) {
        return "release-".concat(deploymentResourceId);
    }

    @Override
    @Transactional
    public void rollback(String deploymentResourceId) {

    }

    @Override
    @Transactional
    public void stop(String releaseResourceId) {
        ReleaseId id = new ReleaseId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setReleaseResourceId(releaseResourceId);

        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Release with id %s not found.", releaseResourceId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Release findById(String releaseResourceId) {
        ReleaseId id = new ReleaseId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setReleaseResourceId(releaseResourceId);
        return releaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Release with id %s not found.", releaseResourceId)));
    }

    @Override
    @Transactional
    public void delete(String releaseResourceId) {
        ReleaseId id = new ReleaseId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setReleaseResourceId(releaseResourceId);

        if (releaseRepository.existsById(id) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Release with id %s not found.", releaseResourceId));
        }

        releaseRepository.deleteById(id);
        pipelineResourceRepository.deleteAllByReleaseResourceId(releaseResourceId);
    }

    @Override
    @Transactional
    public void update(Release release) {

    }

    @Override
    @Transactional
    public Set<Release> listAllInDeployment(String deploymentResourceId) {
        return releaseRepository.findDistinctByDeploymentResourceIdOrderByCreatedOnDesc(deploymentResourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Release> listAllInProject(String projectResourceId) {
        return releaseRepository.findDistinctByProjectResourceId(projectResourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PipelineResource> listAllPipelineResources(String releaseResourceId) {
        ReleaseId id = new ReleaseId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setReleaseResourceId(releaseResourceId);

        if (releaseRepository.existsById(id) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Release with id %s not found.", releaseResourceId));
        }
        return pipelineResourceRepository.findDistinctByReleaseResourceId(releaseResourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public PipelineResource getPipelineResourceById(String pipelineResourceId) {
        PipelineResourceId id = new PipelineResourceId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setGuid(pipelineResourceId);
        return pipelineResourceRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format("Pipeline Resource with id %s not found.", pipelineResourceId)));
    }


    private void deployPipelineResources(List<PipelineResource> pipelineResources, String kubeConfig) {
        pipelineResources.stream()
                .filter(r -> "secret".equalsIgnoreCase(r.getResourceType()))
                .forEach(secret -> {
                    try {
                        KubernetesUtility.createSecretUsingYamlContent(secret.getResourceContent(), "default", "false", kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        //check service-account is not more than one.
        pipelineResources.stream()
                .filter(r -> "service-account".equalsIgnoreCase(r.getResourceType()))
                .forEach(serviceAccount -> {
                    try {
                        KubernetesUtility.createServiceAccountUsingYamlContent(serviceAccount.getResourceContent(), "default", "false", kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> "configmap".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineResource -> {
                    try {
                        KubernetesUtility.createConfigmapUsingYamlContent(pipelineResource.getResourceContent(), "default", "false", kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> "pipeline-resource".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineResource -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(pipelineResource.getResourceContent(), "default", "tekton.dev", "v1alpha1", "pipelineresources", "false", kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> "task".equalsIgnoreCase(r.getResourceType()))
                .forEach(task -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(task.getResourceContent(), "default", "tekton.dev", "v1alpha1", "tasks", "false", kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> "pipeline".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipeline -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(pipeline.getResourceContent(), "default", "tekton.dev", "v1alpha1", "pipelines", "false", kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        //check pipeline-run is not more than one.
        pipelineResources.stream()
                .filter(r -> "pipeline-run".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineRun -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(pipelineRun.getResourceContent(), "default", "tekton.dev", "v1alpha1", "pipelineruns", "false", kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });


    }

    private List<PipelineResource> generatePipelineResources(String pipelineType, DeploymentDetailsDto deploymentDetailsDto, String releaseResourceId) {
        String baseResourcePath = "classpath:/pipeline-templates/sb-standard-dev-pipeline-1.0/";

        Map<String, String> pipelineTemplatingVariables = preparePipelineTemplatingVariables(deploymentDetailsDto, releaseResourceId);

        List<PipelineResource> resources = new ArrayList<>();

        try {
            PipelineResource helmValuesConfigMap = new PipelineResource();
            helmValuesConfigMap.setFormat("yaml");
            helmValuesConfigMap.setResourceType("configmap");

            Map<String, Object> configMapValues = new HashMap<>();
            configMapValues.put("kind", "ConfigMap");
            configMapValues.put("apiVersion", "v1");
            configMapValues.put("metadata", new SingletonMap("name", pipelineTemplatingVariables.get("helmValuesConfigMapName")));
            configMapValues.put("data", new SingletonMap("helmConfig", pipelineTemplatingVariables.get("helmValuesYaml")));

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);
            String templatedContent = yaml.dump(configMapValues);
            System.out.println(templatedContent);
            helmValuesConfigMap.setResourceContent(templatedContent);
            resources.add(helmValuesConfigMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PipelineResource makisuRegistrySecret = new PipelineResource();
            makisuRegistrySecret.setFormat("yaml");
            makisuRegistrySecret.setResourceType("secret");

            Map<String, Object> secretValues = new HashMap<>();
            secretValues.put("kind", "Secret");
            secretValues.put("apiVersion", "v1");
            secretValues.put("type", "Opaque");
            secretValues.put("metadata", new SingletonMap("name", pipelineTemplatingVariables.get("makisuValuesSecretName")));
            secretValues.put("data", new SingletonMap("makisuConfig", pipelineTemplatingVariables.get("makisuValuesYaml")));

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);
            String templatedContent = yaml.dump(secretValues);
            System.out.println(templatedContent);
            makisuRegistrySecret.setResourceContent(templatedContent);
            resources.add(makisuRegistrySecret);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PipelineResource gitSecret = new PipelineResource();
        gitSecret.setFormat("yaml");
        gitSecret.setResourceType("secret");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("git-secret.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            gitSecret.setResourceContent(templatedContent);
            resources.add(gitSecret);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource serviceAccount = new PipelineResource();
        serviceAccount.setFormat("yaml");
        serviceAccount.setResourceType("service-account");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("service-account.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            serviceAccount.setResourceContent(templatedContent);
            resources.add(serviceAccount);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource kubeconfigSecret = new PipelineResource();
        kubeconfigSecret.setFormat("yaml");
        kubeconfigSecret.setResourceType("secret");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("kubeconfig-secret.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            kubeconfigSecret.setResourceContent(templatedContent);
            resources.add(kubeconfigSecret);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource tknPipelineResource = new PipelineResource();
        tknPipelineResource.setFormat("yaml");
        tknPipelineResource.setResourceType("pipeline-resource");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("git-resource.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            tknPipelineResource.setResourceContent(templatedContent);
            resources.add(tknPipelineResource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource makisuBuildTask = new PipelineResource();
        makisuBuildTask.setFormat("yaml");
        makisuBuildTask.setResourceType("task");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("task-makisu.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            makisuBuildTask.setResourceContent(templatedContent);
            resources.add(makisuBuildTask);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource helmDeployTask = new PipelineResource();
        helmDeployTask.setFormat("yaml");
        helmDeployTask.setResourceType("task");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("task-helm.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            helmDeployTask.setResourceContent(templatedContent);
            resources.add(helmDeployTask);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource tknPipeline = new PipelineResource();
        tknPipeline.setFormat("yaml");
        tknPipeline.setResourceType("pipeline");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("pipeline.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            tknPipeline.setResourceContent(templatedContent);
            resources.add(tknPipeline);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource tknPipelineRun = new PipelineResource();
        tknPipelineRun.setFormat("yaml");
        tknPipelineRun.setResourceType("pipeline-run");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("pipeline-run.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            tknPipelineRun.setResourceContent(templatedContent);
            resources.add(tknPipelineRun);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return resources;
    }

    public String getPipelineTemplateContent(String templatePath) throws IOException {
        Resource resource = resourceLoader.getResource(templatePath);
        return FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);
    }

    public Map<String, String> preparePipelineTemplatingVariables(DeploymentDetailsDto deploymentDetailsDto, String releaseResourceId) {
        String deploymentResourceId = deploymentDetailsDto.getDeploymentId().getDeploymentResourceId();
        Map<String, String> args = new HashMap<>();

        //git resource
        args.put("gitResourceName", "git-resource-".concat(releaseResourceId));
        args.put("gitRepoUrl", deploymentDetailsDto.getGitRepoUrl());
        args.put("gitRepoBranchName", deploymentDetailsDto.getGitRepoBranchName());

        //git secret
        args.put("gitRepoSecretName", "git-secret-".concat(releaseResourceId));
        args.put("gitRepoBaseUrl", "https://gitlab.com");
        args.put("gitRepoUsername", deploymentDetailsDto.getGitRepoUsername());
        args.put("gitRepoPassword", deploymentDetailsDto.getGitRepoPassword());

        //kubeconfig secret
        args.put("kubeConfigSecretName", "kubeconfig-secret-".concat(releaseResourceId));
        args.put("kubeConfigBase64", deploymentDetailsDto.getDevKubeconfig());


        //service account
        args.put("serviceAccountName", "service-account-".concat(releaseResourceId));
        //also has "gitRepoSecretName" which is already added.

        //helm values config map
        args.put("helmValuesConfigMapName", "configmap-helm-values-".concat(releaseResourceId));
        args.put("helmValuesYaml", getHelmValuesYaml(deploymentDetailsDto, releaseResourceId));

        //makisu values config map
        args.put("makisuValuesSecretName", "secret-makisu-values-".concat(releaseResourceId));
        args.put("makisuValuesYaml", getMakisuRegistryConfig(deploymentDetailsDto, releaseResourceId));

        //task helm
        args.put("helmDeployTaskName", "task-helm-deploy-".concat(releaseResourceId));
        //also has helmValuesConfigMapName which is already added.

        //task makisu
        args.put("makisuBuildImageTaskName", "task-makisu-build-".concat(releaseResourceId));
        //also has "gitResourceName" which is already added.

        //pipeline
        args.put("pipelineName", "pipeline-".concat(releaseResourceId));
        args.put("helmReleaseName", getHelmReleaseId(deploymentResourceId));
        args.put("helmCommand", getHelmCommand(deploymentResourceId));
        args.put("helmChartUrl", "https://ashimusmani.github.io/helm-charts/basic-springboot-demo-ketchup-0.1.0.tgz");
        args.put("containerRegistryUrl", deploymentDetailsDto.getContainerRegistryUrl());

        String imageTag = getImageTagName(deploymentDetailsDto, releaseResourceId);
        args.put("imageTag", imageTag);

        String registryPassword;
        if ("gcr".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            JSONObject gcrRegistryKey = new JSONObject(deploymentDetailsDto.getContainerRegistryPassword());
            System.out.println("escaped gcr key: " + gcrRegistryKey.toString());
            registryPassword = gcrRegistryKey.toString();
        } else {
            registryPassword = deploymentDetailsDto.getContainerRegistryPassword();
        }

//        Map<String, String> registryConfigArgs = new HashMap<>();
//        registryConfigArgs.put("registryUrl", deploymentDetailsDto.getContainerRegistryUrl());
//        registryConfigArgs.put("registryUsername", deploymentDetailsDto.getContainerRegistryUsername());
//        registryConfigArgs.put("registryPassword", registryPassword);
//        StrSubstitutor sub = new StrSubstitutor(registryConfigArgs, "${", "}");
//        String makisuImageRegistryConfig = sub.replace(getMakisuRegistryConfigTemplate(deploymentDetailsDto.getContainerRegistryType()));

        args.put("imageRegistryConfig", "makisuImageRegistryConfig");
        args.put("devKubernetesNamespace", deploymentDetailsDto.getDevKubernetesNamespace());
        //also has "gitResourceName", "makisuBuildImageTaskName", "helmDeployTaskName"  which are already added.

        //pipeline run
        args.put("pipelineRunName", "pipeline-run-".concat(releaseResourceId));
        //also has "serviceAccountName", "pipelineName", "gitResourceName" which are already added.

        return args;
    }

    public String getHelmCommand(String deploymentResourceId) {
        long noOfReleases = releaseRepository.countAllByDeploymentResourceId(deploymentResourceId);
        if (noOfReleases > 0) {
            return "upgrade";
        } else {
            return "install";
        }
    }


    public String getMakisuRegistryConfigTemplate(String registryType) {
        String template = "";
        if ("local".equalsIgnoreCase(registryType)) {
            template = "'{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}}}}}}'";
        } else if ("docker-hub".equalsIgnoreCase(registryType)) {
            template = "'{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}},\"basic\":{\"username\":\"${registryUsername}\",\"password\":\"${registryPassword}\"}}}}}'";
        } else if ("gcr".equalsIgnoreCase(registryType)) {
            template = "'{\"${registryUrl}\":{\"ketchup-test/*\":{\"push_chunk\": -1, \"security\":{\"tls\":{\"client\":{\"disabled\":false}},\"basic\":{\"username\":\"${registryUsername}\",\"password\": {${registryPassword}}}}}}}'";
        } else {
            throw new RuntimeException("Unknown registry type supported types are local, docker-hub, aws-ecr, gcr and azurecr.");
        }
        return template;
    }

    public String getImageTagName(DeploymentDetailsDto deploymentDetailsDto, String releaseResourceId) {
        String imageTag = "";
        if ("local".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            if ("".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryPath())) {
                imageTag = deploymentDetailsDto.getContainerRegistryUrl() + "/" + releaseResourceId;
            } else {
                imageTag = deploymentDetailsDto.getContainerRegistryUrl()
                        + "/" + deploymentDetailsDto.getContainerRegistryPath() + "/" + releaseResourceId;
            }
        } else if ("docker-hub".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            //docker hub doesnt have different images in repository, repository name and image name should be same.
            imageTag = deploymentDetailsDto.getContainerRegistryUrl()
                    + "/" + deploymentDetailsDto.getContainerRegistryUsername()
                    + "/" + releaseResourceId;
        } else if ("gcr".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            //gcr has project id as mandatory part and no nesting is allowed not even single level.
            imageTag = deploymentDetailsDto.getContainerRegistryUrl()
                    + "/" + deploymentDetailsDto.getContainerRegistryPath()
                    + "/" + releaseResourceId;
        } else {
            throw new RuntimeException("Unknown registry type supported types are local, docker-hub, aws-ecr, gcr and azurecr.");
        }
        return imageTag;
    }

    public String getTemplatedPipelineResource(String template, Map<String, String> templatingVariables) {
        StrSubstitutor sub = new StrSubstitutor(templatingVariables, "${", "}");
        String templatedContent = sub.replace(template);
        System.out.println(templatedContent);
        return templatedContent;
    }

    public String getHelmValuesYaml(DeploymentDetailsDto deploymentDetailsDto, String releaseResourceId) {
        LinkedHashMap<String, Object> containerRegistryValues = new LinkedHashMap<>();
        containerRegistryValues.put("repository", getImageTagName(deploymentDetailsDto, releaseResourceId));

        LinkedHashMap<String, Object> serviceValues = new LinkedHashMap<>();
        serviceValues.put("type", "ClusterIP");
        serviceValues.put("port", Long.valueOf(deploymentDetailsDto.getAppServerPort()));


        LinkedHashMap<String, Object> helmConfigValues = new LinkedHashMap<>();
        helmConfigValues.put("replicaCount", Long.valueOf(deploymentDetailsDto.getReplicas()));
        helmConfigValues.put("image", containerRegistryValues);
        helmConfigValues.put("service", serviceValues);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml helmConfigYaml = new Yaml(options);
        String helmConfigString = helmConfigYaml.dump(helmConfigValues);
        System.out.println(helmConfigString);
        return helmConfigString;
    }


// local:      "'{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}}}}}}'"
// docker-hub: "'{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}},\"basic\":{\"username\":\"${registryUsername}\",\"password\":\"${registryPassword}\"}}}}}'"
// gcr:        "'{\"${registryUrl}\":{\"ketchup-test/*\":{\"push_chunk\": -1, \"security\":{\"tls\":{\"client\":{\"disabled\":false}},\"basic\":{\"username\":\"${registryUsername}\",\"password\": {${registryPassword}}}}}}}'"

    public String getMakisuRegistryConfig(DeploymentDetailsDto deploymentDetailsDto, String releaseResourceId) {
        if ("local".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            LinkedHashMap<String, Object> containerRegistryValues = new LinkedHashMap<>();
            containerRegistryValues.put("security", new SingletonMap("tls", new SingletonMap("client", new SingletonMap("disabled", false))));

            LinkedHashMap<String, Object> serviceValues = new LinkedHashMap<>();
            serviceValues.put(".*", containerRegistryValues);

//            String registryUrl = "";
//            if ("".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryPath())) {
//                registryUrl = deploymentDetailsDto.getContainerRegistryUrl() + "/" + releaseResourceId;
//            } else {
//                registryUrl = deploymentDetailsDto.getContainerRegistryUrl()
//                        + "/" + deploymentDetailsDto.getContainerRegistryPath() + "/" + releaseResourceId;
//            }
            String registryUrl = deploymentDetailsDto.getContainerRegistryUrl();
            SingletonMap conf = new SingletonMap(registryUrl, serviceValues);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml makisuRegistryConfigYaml = new Yaml(options);
            String makisuRegistryConfigString = makisuRegistryConfigYaml.dump(conf);
            System.out.println(makisuRegistryConfigString);
            String encodedConfig = Base64.getEncoder().encodeToString(makisuRegistryConfigString.getBytes());
            return encodedConfig;
        } else if ("docker-hub".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            //docker hub doesnt have different images in repository, repository name and image name should be same.
            LinkedHashMap<String, Object> creds = new LinkedHashMap<>();
            creds.put("username", deploymentDetailsDto.getContainerRegistryUsername());
            creds.put("password", deploymentDetailsDto.getContainerRegistryPassword());

//            String registryUrl = deploymentDetailsDto.getContainerRegistryUrl();
            String registryUrl = "index.docker.io";
            SingletonMap conf = new SingletonMap(registryUrl, new SingletonMap(".*", new SingletonMap("security", new SingletonMap("basic", creds))));

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml makisuRegistryConfigYaml = new Yaml(options);
            String makisuRegistryConfigString = makisuRegistryConfigYaml.dump(conf);
            System.out.println(makisuRegistryConfigString);
            String encodedConfig = Base64.getEncoder().encodeToString(makisuRegistryConfigString.getBytes());
            return encodedConfig;
        } else if ("gcr".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            //gcr has project id as mandatory part and no nesting is allowed not even single level.
            LinkedHashMap<String, Object> creds = new LinkedHashMap<>();
            creds.put("username", deploymentDetailsDto.getContainerRegistryUsername());
            creds.put("password", deploymentDetailsDto.getContainerRegistryPassword());

            LinkedHashMap<String, Object> containerRegistryValues = new LinkedHashMap<>();
            containerRegistryValues.put("push_chunk", -1);
            containerRegistryValues.put("security", new SingletonMap("basic", creds));

            LinkedHashMap<String, Object> serviceValues = new LinkedHashMap<>();
//            serviceValues.put("ketchup-test/*", containerRegistryValues);
            serviceValues.put(".*", containerRegistryValues);

//            String registryUrl = deploymentDetailsDto.getContainerRegistryUrl();
            String registryUrl = "gcr.io";
            SingletonMap conf = new SingletonMap(registryUrl, serviceValues);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml makisuRegistryConfigYaml = new Yaml(options);
            String makisuRegistryConfigString = makisuRegistryConfigYaml.dump(conf);
            System.out.println(makisuRegistryConfigString);
            String encodedConfig = Base64.getEncoder().encodeToString(makisuRegistryConfigString.getBytes());
            return encodedConfig;
        } else {
            throw new RuntimeException("Unknown registry type supported types are local, docker-hub, aws-ecr, gcr and azurecr.");
        }


    }
}
