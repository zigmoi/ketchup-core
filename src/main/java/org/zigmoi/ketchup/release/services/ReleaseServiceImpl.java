package org.zigmoi.ketchup.release.services;

import io.kubernetes.client.openapi.ApiException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.deployment.dtos.DeploymentDetailsDto;
import org.zigmoi.ketchup.deployment.services.DeploymentService;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;
import org.zigmoi.ketchup.project.services.ProjectSettingsService;
import org.zigmoi.ketchup.release.entities.PipelineResource;
import org.zigmoi.ketchup.release.entities.PipelineResourceId;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;
import org.zigmoi.ketchup.release.repositories.PipelineResourceRepository;
import org.zigmoi.ketchup.release.repositories.ReleaseRepository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class ReleaseServiceImpl extends TenantProviderService implements ReleaseService {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseServiceImpl.class);

    private final ReleaseRepository releaseRepository;

    private final PipelineResourceRepository pipelineResourceRepository;

    private PermissionUtilsService permissionUtilsService;

    private DeploymentService deploymentService;

    private ProjectSettingsService projectSettingsService;

    @Autowired
    public ReleaseServiceImpl(ReleaseRepository releaseRepository, PipelineResourceRepository pipelineResourceRepository, PermissionUtilsService permissionUtilsService, DeploymentService deploymentService, ProjectSettingsService projectSettingsService) {
        this.releaseRepository = releaseRepository;
        this.pipelineResourceRepository = pipelineResourceRepository;
        this.permissionUtilsService = permissionUtilsService;
        this.deploymentService = deploymentService;
        this.projectSettingsService = projectSettingsService;
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
        releaseId.setReleaseResourceId(UUID.randomUUID().toString());
        releaseId.setTenantId(tenantId);
        r.setId(releaseId);
        r.setDeploymentResourceId(deploymentResourceId);
        r.setProjectResourceId(projectResourceId);

        //generate helm values yaml and save it.
//      String containerRepoUrl = "191.101.165.0:5000";
        String containerRepoUrl = deploymentDetailsDto.getContainerRegistryUrl();

        String containerRepoFullName = containerRepoUrl + "/" + deploymentDetailsDto.getContainerRegistryPath();
        LinkedHashMap<String, Object> containerRegistryValues = new LinkedHashMap<>();
        containerRegistryValues.put("repository", containerRepoFullName);


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
        r.setHelmValuesYaml(helmConfigString);

        Map<String, String> args = new HashMap<>();
        args.put("registryUrl", containerRepoUrl);
        StrSubstitutor sub = new StrSubstitutor(args, "${", "}");
        String makisuImageRegistryConfig = sub.replace("{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}}}}}}");
        System.out.println(makisuImageRegistryConfig);
        r.setMakisuImageRegistryConfigJson(makisuImageRegistryConfig);


        //generate pipeline all resources and save them, name can be parsed from them directly.
//        List<PipelineResource> pipelineResources = getAllPipelineResources("standard-sb-1");
        List<PipelineResource> pipelineResources = generatePipelineResources("sb-standard-dev-1.0", deploymentDetailsDto, releaseId.getReleaseResourceId());
        pipelineResources.stream().forEach(pipelineResource ->
        {
            PipelineResourceId pipelineResourceId = new PipelineResourceId();
            pipelineResourceId.setTenantId(tenantId);
            pipelineResourceId.setGuid(UUID.randomUUID().toString());
            pipelineResource.setId(pipelineResourceId);
            pipelineResource.setProjectResourceId(projectResourceId);
            pipelineResource.setReleaseResourceId(releaseId.getReleaseResourceId());
        });
        pipelineResourceRepository.saveAll(pipelineResources);
        releaseRepository.save(r);

        String kubeConfig = StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig());
        deployPipelineResources(pipelineResources, kubeConfig);
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


    private List<PipelineResource> getAllPipelineResources(String pipelineType) {
        String baseResourcePath = "/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/";

        List<PipelineResource> resources = new ArrayList<>();

        PipelineResource r1 = new PipelineResource();
        r1.setFormat("yaml");
        r1.setResourceType("secret");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("secrets.yaml")), UTF_8);
            r1.setResourceContent(content);
            resources.add(r1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r2 = new PipelineResource();
        r2.setFormat("yaml");
        r2.setResourceType("service-account");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("service-account.yaml")), UTF_8);
            r2.setResourceContent(content);
            resources.add(r2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r3 = new PipelineResource();
        r3.setFormat("yaml");
        r3.setResourceType("pipeline-resource");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("resource.yaml")), UTF_8);
            r3.setResourceContent(content);
            resources.add(r3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r4 = new PipelineResource();
        r4.setFormat("yaml");
        r4.setResourceType("task");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("task-makisu.yaml")), UTF_8);
            r4.setResourceContent(content);
            resources.add(r4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r5 = new PipelineResource();
        r5.setFormat("yaml");
        r5.setResourceType("task");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("task-helm.yaml")), UTF_8);
            r5.setResourceContent(content);
            resources.add(r5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r6 = new PipelineResource();
        r6.setFormat("yaml");
        r6.setResourceType("pipeline");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("pipeline.yaml")), UTF_8);
            r6.setResourceContent(content);
            resources.add(r6);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r7 = new PipelineResource();
        r7.setFormat("yaml");
        r7.setResourceType("pipeline-run");
        try {
            String content = FileUtils.readFileToString(new File(baseResourcePath.concat("pipeline-run.yaml")), UTF_8);
            r7.setResourceContent(content);
            resources.add(r7);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resources;
    }

    private List<PipelineResource> generatePipelineResources(String pipelineType, DeploymentDetailsDto deploymentDetailsDto, String releaseResourceId) {
        String baseResourcePath = "/Users/neo/Documents/dev/java/ketchup-core/src/main/resources/pipeline-templates/sb-standard-dev-pipeline-1.0/";

        Map<String, String> pipelineTemplatingVariables = preparePipelineTemplatingVariables(deploymentDetailsDto, releaseResourceId);

        List<PipelineResource> resources = new ArrayList<>();

        PipelineResource r1 = new PipelineResource();
        r1.setFormat("yaml");
        r1.setResourceType("secret");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("git-secret.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            r1.setResourceContent(templatedContent);
            resources.add(r1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r2 = new PipelineResource();
        r2.setFormat("yaml");
        r2.setResourceType("service-account");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("service-account.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            r2.setResourceContent(templatedContent);
            resources.add(r2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r3 = new PipelineResource();
        r3.setFormat("yaml");
        r3.setResourceType("pipeline-resource");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("git-resource.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            r3.setResourceContent(templatedContent);
            resources.add(r3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r4 = new PipelineResource();
        r4.setFormat("yaml");
        r4.setResourceType("task");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("task-makisu.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            r4.setResourceContent(templatedContent);
            resources.add(r4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r5 = new PipelineResource();
        r5.setFormat("yaml");
        r5.setResourceType("task");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("task-helm.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            r5.setResourceContent(templatedContent);
            resources.add(r5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r6 = new PipelineResource();
        r6.setFormat("yaml");
        r6.setResourceType("pipeline");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("pipeline.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            r6.setResourceContent(templatedContent);
            resources.add(r6);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PipelineResource r7 = new PipelineResource();
        r7.setFormat("yaml");
        r7.setResourceType("pipeline-run");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("pipeline-run.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            r7.setResourceContent(templatedContent);
            resources.add(r7);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resources;
    }

    public String getPipelineTemplateContent(String templatePath) throws IOException {
        return FileUtils.readFileToString(new File(templatePath), StandardCharsets.UTF_8);
    }

    public Map<String, String> preparePipelineTemplatingVariables(DeploymentDetailsDto deploymentDetailsDto, String releaseResourceId) {
        Map<String, String> args = new HashMap<>();

        //git resource
        String gitResourceName = "git-resource-".concat(releaseResourceId);
        args.put("gitResourceName", gitResourceName);
        args.put("gitRepoUrl", deploymentDetailsDto.getGitRepoUrl());
        args.put("gitRepoBranchName", deploymentDetailsDto.getGitRepoBranchName());

        //git secret
        String gitSecretName = "git-secret-".concat(releaseResourceId);
        args.put("gitRepoSecretName", gitSecretName);
        args.put("gitRepoBaseUrl", "https://gitlab.com");
        args.put("gitRepoUsername", deploymentDetailsDto.getGitRepoUsername());
        args.put("gitRepoPassword", deploymentDetailsDto.getGitRepoPassword());

        //service account
        String serviceAccountName = "service-account-".concat(releaseResourceId);
        args.put("serviceAccountName", serviceAccountName);
        //also has "gitRepoSecretName" which is already added.

        //task helm
        String helmTaskName = "task-helm-deploy-".concat(releaseResourceId);
        args.put("helmDeployTaskName", helmTaskName);

        //task makisu
        String makisuTaskName = "task-makisu-build-".concat(releaseResourceId);
        args.put("makisuBuildImageTaskName", makisuTaskName);
        //also has "gitResourceName" which is already added.

        //pipeline
        String pipelineName = "pipeline-".concat(releaseResourceId);
        args.put("pipelineName", pipelineName);
        String helmReleaseName = "release-".concat(releaseResourceId);
        args.put("helmReleaseName", helmReleaseName);
        args.put("containerRegistryUrl", deploymentDetailsDto.getContainerRegistryUrl());
        String imageTag = "";
        if ("local".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            imageTag = deploymentDetailsDto.getContainerRegistryUrl() + "/" + releaseResourceId;
        } else {
            imageTag = deploymentDetailsDto.getContainerRegistryUrl() + "/" + releaseResourceId;
        }
        args.put("imageTag", imageTag);

        Map<String, String> args1 = new HashMap<>();
        args1.put("registryUrl", deploymentDetailsDto.getContainerRegistryUrl());
        StrSubstitutor sub = new StrSubstitutor(args1, "${", "}");
        String makisuImageRegistryConfig = sub.replace("'{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}}}}}}'");

        args.put("imageRegistryConfig", makisuImageRegistryConfig);

        args.put("devKubernetesNamespace", deploymentDetailsDto.getDevKubernetesNamespace());
        //also has "gitResourceName", "makisuBuildImageTaskName", "helmDeployTaskName"  which are already added.

        //pipeline run
        String pipelineRunName = "pipeline-run-".concat(releaseResourceId);
        args.put("pipelineRunName", pipelineRunName);
        //also has "serviceAccountName", "pipelineName", "gitResourceName" which are already added.

        return args;
    }

    public String getTemplatedPipelineResource(String template, Map<String, String> templatingVariables) {
        StrSubstitutor sub = new StrSubstitutor(templatingVariables, "${", "}");
        String templatedContent = sub.replace(template);
        System.out.println(templatedContent);
        return templatedContent;
    }
}
