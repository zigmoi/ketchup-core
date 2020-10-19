package org.zigmoi.ketchup.release.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.internal.LinkedTreeMap;
import io.kubernetes.client.openapi.ApiException;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.zigmoi.ketchup.common.ConfigUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.deployment.dtos.DeploymentDetailsDto;
import org.zigmoi.ketchup.deployment.services.DeploymentService;
import org.zigmoi.ketchup.exception.UnexpectedException;
import org.zigmoi.ketchup.helm.services.HelmService;
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
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.zigmoi.ketchup.deployment.DeploymentConstants.*;

@Service
public class ReleaseServiceImpl extends TenantProviderService implements ReleaseService {
    private static final Logger logger = LoggerFactory.getLogger(ReleaseServiceImpl.class);
    private static final Map<String, Boolean> tektonConfigAppliedToKubernetesCluster = new ConcurrentHashMap<>();
    private final ReleaseRepository releaseRepository;
    private final PipelineResourceRepository pipelineResourceRepository;
    private final PermissionUtilsService permissionUtilsService;
    private final DeploymentService deploymentService;
    private final ResourceLoader resourceLoader;
    private final AuthorizationServerTokenServices jwtTokenServices;
    private final HelmService helmService;

    public ReleaseServiceImpl(ReleaseRepository releaseRepository, PipelineResourceRepository pipelineResourceRepository, PermissionUtilsService permissionUtilsService, DeploymentService deploymentService, ResourceLoader resourceLoader, AuthorizationServerTokenServices jwtTokenServices, HelmService helmService) {
        this.releaseRepository = releaseRepository;
        this.pipelineResourceRepository = pipelineResourceRepository;
        this.permissionUtilsService = permissionUtilsService;
        this.deploymentService = deploymentService;
        this.resourceLoader = resourceLoader;
        this.jwtTokenServices = jwtTokenServices;
        this.helmService = helmService;
    }

    private static String getNamespaceForTektonPipeline(String devKubernetesClusterSettingId) {
        return "tekton-pipelines";
    }

    private synchronized void checkAndApplyTektonCloudEventURL(DeploymentDetailsDto deploymentDetailsDto, Release release, String kubeConfig) throws IOException, ApiException {
        if (!(tektonConfigAppliedToKubernetesCluster.containsKey(deploymentDetailsDto.getDevKubernetesClusterSettingId())
                && tektonConfigAppliedToKubernetesCluster.get(deploymentDetailsDto.getDevKubernetesClusterSettingId()))) {
            String templatedContent = "";
            try {
                PipelineResource tektonCloudEventSinkConfig = new PipelineResource();
                tektonCloudEventSinkConfig.setFormat("yaml");
                tektonCloudEventSinkConfig.setResourceType("configmap");

                Map<String, Object> labels = new HashMap<>();
                labels.put("app.kubernetes.io/instance", "default");
                labels.put("app.kubernetes.io/part-of", "tekton-pipelines");

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("name", "config-defaults");
                metadata.put("namespace", getNamespaceForTektonPipeline(deploymentDetailsDto.getDevKubernetesClusterSettingId()));
                metadata.put("labels", labels);

                Map<String, Object> configMapValues = new HashMap<>();
                configMapValues.put("kind", "ConfigMap");
                configMapValues.put("apiVersion", "v1");
                configMapValues.put("metadata", metadata);
                configMapValues.put("data", new SingletonMap("default-cloud-events-sink", buildTektonCloudEventSinkURL(release)));

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setPrettyFlow(true);
                Yaml yaml = new Yaml(options);
                templatedContent = yaml.dump(configMapValues);
                System.out.println(templatedContent);
                tektonCloudEventSinkConfig.setResourceContent(templatedContent);
                KubernetesUtility.createConfigmapUsingYamlContent(templatedContent,
                        getNamespaceForTektonPipeline(deploymentDetailsDto.getDevKubernetesClusterSettingId()), "false", kubeConfig);
            } catch (ApiException e) {
                if (e.getMessage().toLowerCase().contains("conflict")) {
                    KubernetesUtility.updateConfigmapUsingYamlContent("config-defaults",
                            getNamespaceForTektonPipeline(deploymentDetailsDto.getDevKubernetesClusterSettingId()), templatedContent, kubeConfig);
                }
            } catch (IOException e) {
                throw new UnexpectedException(e.getMessage(), e);
            }

            tektonConfigAppliedToKubernetesCluster.put(deploymentDetailsDto.getDevKubernetesClusterSettingId(), true);
        }
    }

    @Override
    @Transactional
    public String create(String deploymentResourceId) {
        //validate and fetch deploymentId.
        //get projectId from deployment.
        //handle duplicate pipeline resources error in k8s.
        DeploymentDetailsDto deploymentDetailsDto = deploymentService.getDeployment(deploymentResourceId);
        long noOfReleases = releaseRepository.countAllByDeploymentResourceId(deploymentResourceId);
        String releaseVersion = "v".concat(String.valueOf(noOfReleases + 1));

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
        r.setVersion(releaseVersion);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            r.setDeploymentDataJson(objectMapper.writeValueAsString(deploymentDetailsDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        //generate pipeline all resources and save them, name can be parsed from them directly.
        List<PipelineResource> pipelineResources = generatePipelineResources_tekton_v1beta1("sb-standard-dev-1.0",
                deploymentDetailsDto, releaseResourceId, releaseVersion);
        pipelineResources.forEach(pipelineResource -> {
            PipelineResourceId pipelineResourceId = new PipelineResourceId();
            pipelineResourceId.setTenantId(tenantId);
            pipelineResourceId.setGuid(UUID.randomUUID().toString());
            pipelineResource.setId(pipelineResourceId);
            pipelineResource.setProjectResourceId(projectResourceId);
            pipelineResource.setReleaseResourceId(releaseResourceId);
        });
        pipelineResourceRepository.saveAll(pipelineResources);
        releaseRepository.saveAndFlush(r);

        String kubeConfig = StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig());
        queueAndDeployPipelineResources(pipelineResources, kubeConfig, deploymentDetailsDto, r);
        return releaseResourceId;
    }

    private void queueAndDeployPipelineResources(List<PipelineResource> pipelineResources, String kubeConfig,
                                                 DeploymentDetailsDto deploymentDetailsDto, Release r) {
        deployPipelineResources(pipelineResources, kubeConfig, deploymentDetailsDto, r);
    }

    private String getHelmReleaseId(String deploymentResourceId) {
        return "release-".concat(deploymentResourceId);
    }

    @Override
    @Transactional
    public void rollback(String releaseResourceId) {
        ReleaseId id = new ReleaseId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setReleaseResourceId(releaseResourceId);

        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Release with id %s not found.", releaseResourceId)));
        String deploymentResourceId = release.getDeploymentResourceId();
        DeploymentDetailsDto deploymentDetails = deploymentService.getDeployment(deploymentResourceId);
        String namespace = deploymentDetails.getDevKubernetesNamespace();
        String kubeConfig = StringUtility.decodeBase64(deploymentDetails.getDevKubeconfig());
        String releaseName = getHelmReleaseId(deploymentDetails.getDeploymentId().getDeploymentResourceId());
        String revision = "1";
        helmService.rollbackRelease(releaseName, revision, namespace, kubeConfig);
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
        String deploymentResourceId = release.getDeploymentResourceId();
        DeploymentDetailsDto deploymentDetails = deploymentService.getDeployment(deploymentResourceId);
        String namespace = deploymentDetails.getDevKubernetesNamespace();
        //check if release is not failed/success than cancel the pipeline.
        PipelineResource resource = pipelineResourceRepository.findByReleaseResourceIdAndResourceType(releaseResourceId, "pipeline-run").orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                String.format("Pipeline run for release with id %s not found.", releaseResourceId)));

        Yaml yaml = new Yaml();
        LinkedHashMap<String, Object> pipelineRun = (LinkedHashMap<String, Object>) yaml.loadAs(resource.getResourceContent(), Map.class);
        LinkedHashMap<String, Object> metadata = (LinkedHashMap<String, Object>) pipelineRun.get("metadata");
        String resourceName = (String) metadata.get("name");

        ObjectMapper objectMapper = new ObjectMapper();
        DeploymentDetailsDto deploymentDetailsDto = null;
        try {
            deploymentDetailsDto = objectMapper.readValue(release.getDeploymentDataJson(), DeploymentDetailsDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String kubeConfig = StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig());
        try {
            LinkedTreeMap<String, Object> latestPipelineRun = KubernetesUtility.getCRD(resourceName, namespace, "tekton.dev", "v1beta1", "pipelineruns", kubeConfig);
            ((LinkedTreeMap<String, Object>) latestPipelineRun.get("spec")).put("status", "PipelineRunCancelled");

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml updatedYaml = new Yaml(options);
            String cancelledPipelineRun = updatedYaml.dump(latestPipelineRun);
            System.out.println(cancelledPipelineRun);

            KubernetesUtility.updateCRDUsingYamlContent(resourceName, cancelledPipelineRun, namespace, "tekton.dev", "v1beta1", "pipelineruns", kubeConfig);
        } catch (IOException | ApiException e) {
            logger.error("Failed to cancel pipeline, ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cancel pipeline.");
        }
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
    public void deleteDeployment(String projectResourceId, String deploymentResourceId) {
        Optional<Release> activeRelease = getActiveRelease(deploymentResourceId);
        if (activeRelease.isPresent()) {
            DeploymentDetailsDto deploymentDetailsDto = deploymentJsonToDto(activeRelease.get().getDeploymentDataJson());
            String namespace = deploymentDetailsDto.getDevKubernetesNamespace();
            String kubeConfig = StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig());
            helmService.uninstallChart("release-" + deploymentResourceId, namespace, kubeConfig);
        } else {
            System.out.println("No active release found to uninstall.");
        }

        List<Release> releases = releaseRepository.findAllByDeploymentResourceId(deploymentResourceId);
        releases.forEach(release -> pipelineResourceRepository.deleteAllByReleaseResourceId(release.getId().getReleaseResourceId()));
        releaseRepository.deleteAllByDeploymentResourceId(deploymentResourceId);
        deploymentService.deleteDeployment(projectResourceId, deploymentResourceId);
    }

    public DeploymentDetailsDto deploymentJsonToDto(String deploymentJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        DeploymentDetailsDto deploymentDetailsDto = null;
        try {
            deploymentDetailsDto = objectMapper.readValue(deploymentJson, DeploymentDetailsDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deploymentDetailsDto;
    }

    @Override
    @Transactional
    public void update(Release release) {

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Release> getActiveRelease(String deploymentResourceId) {
        return releaseRepository.findTopByDeploymentResourceIdAndStatusOrderByLastUpdatedOnDesc(deploymentResourceId, "SUCCESS");
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
    public Set<Release> listRecentInProject(String projectResourceId) {
        return releaseRepository.findDistinctTop5ByProjectResourceIdOrderByLastUpdatedOnDesc(projectResourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Release> listAllInProjectWithStatus(String projectResourceId, String status) {
        return releaseRepository.findDistinctByProjectResourceIdAndStatusOrderByLastUpdatedOnDesc(projectResourceId, status);
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

    private void deployPipelineResources(List<PipelineResource> pipelineResources, String kubeConfig, DeploymentDetailsDto deploymentDetailsDto, Release release) {
        try {
            checkAndApplyTektonCloudEventURL(deploymentDetailsDto, release, kubeConfig);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        String namespace = deploymentDetailsDto.getDevKubernetesNamespace();
        pipelineResources.stream()
                .filter(r -> "pipeline-pvc".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineResource -> {
                    try {
                        KubernetesUtility.createPvcUsingYamlContent(pipelineResource.getResourceContent(), namespace, "false", kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> "secret".equalsIgnoreCase(r.getResourceType()))
                .forEach(secret -> {
                    try {
                        KubernetesUtility.createSecretUsingYamlContent(secret.getResourceContent(), namespace, "false", kubeConfig);
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
                        KubernetesUtility.createServiceAccountUsingYamlContent(serviceAccount.getResourceContent(), namespace, "false", kubeConfig);
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
                        KubernetesUtility.createConfigmapUsingYamlContent(pipelineResource.getResourceContent(), namespace, "false", kubeConfig);
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
                        KubernetesUtility.createCRDUsingYamlContent(task.getResourceContent(), namespace, "tekton.dev", "v1beta1", "tasks", "false", kubeConfig);
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
                        KubernetesUtility.createCRDUsingYamlContent(pipeline.getResourceContent(), namespace, "tekton.dev", "v1beta1", "pipelines", "false", kubeConfig);
                    } catch (IOException | ApiException e) {
                        e.printStackTrace();
                    }
                });

        //check pipeline-run is not more than one.
        pipelineResources.stream()
                .filter(r -> "pipeline-run".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineRun -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(pipelineRun.getResourceContent(), namespace, "tekton.dev", "v1beta1", "pipelineruns", "false", kubeConfig);
                    } catch (IOException | ApiException e) {
                        e.printStackTrace();
                    }
                });
    }

    private String buildTektonCloudEventSinkURL(Release release) {
        String domain = ConfigUtility.instance().getProperty("ketchup.base-url");
        String tektonEventSink = ConfigUtility.instance().getProperty("ketchup.tekton-event-sink-api-path");
        String accessToken = generateForeverActiveToken(jwtTokenServices, "tekton-event");
        return domain + "/" + tektonEventSink + "?access_token=" + accessToken;
    }

    private void deletePipelineResources(DeploymentDetailsDto deploymentDetailsDto, Collection<PipelineResource> pipelineResources, String kubeConfig) {

        String namespace = deploymentDetailsDto.getDevKubernetesNamespace();
        pipelineResources.stream()
                .filter(r -> Objects.equals("pipeline-run", r.getResourceType().toLowerCase()))
                .forEach(task -> {
                    try {
                        KubernetesUtility.deleteCRD(getPipelineResourceName(task.getResourceContent()),
                                namespace, "tekton.dev", "v1beta1",
                                getPluralForResourceType(task.getResourceType()), 0, kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> Objects.equals("pipeline", r.getResourceType().toLowerCase()))
                .forEach(task -> {
                    try {
                        KubernetesUtility.deleteCRD(getPipelineResourceName(task.getResourceContent()),
                                namespace, "tekton.dev", "v1beta1",
                                getPluralForResourceType(task.getResourceType()), 0, kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> Objects.equals("task", r.getResourceType().toLowerCase()))
                .forEach(task -> {
                    try {
                        KubernetesUtility.deleteCRD(getPipelineResourceName(task.getResourceContent()),
                                namespace, "tekton.dev", "v1beta1",
                                getPluralForResourceType(task.getResourceType()), 0, kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });


        pipelineResources.stream()
                .filter(r -> "secret".equalsIgnoreCase(r.getResourceType()))
                .forEach(secret -> {
                    try {
                        KubernetesUtility.deleteSecret(getPipelineResourceName(secret.getResourceContent()),
                                namespace, 10, kubeConfig);
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
                        KubernetesUtility.deleteServiceAccount(getPipelineResourceName(serviceAccount.getResourceContent()),
                                namespace, 10, kubeConfig);
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
                        KubernetesUtility.deleteConfigMap(getPipelineResourceName(pipelineResource.getResourceContent()),
                                namespace, 10, kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineResources.stream()
                .filter(r -> "pipeline-pvc".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineResource -> {
                    try {
                        KubernetesUtility.deletePvc(getPipelineResourceName(pipelineResource.getResourceContent()),
                                namespace, 10, kubeConfig);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private String getPluralForResourceType(String resourceType) {
        switch (resourceType.toLowerCase()) {
            case "task":
                return "tasks";
            case "pipeline":
                return "pipelines";
            case "pipeline-run":
                return "pipelineruns";
        }
        throw new UnexpectedException("No matching plural found for pipeline resource type : " + resourceType);
    }

    private List<PipelineResource> generatePipelineResources_tekton_v1beta1(String pipelineType, DeploymentDetailsDto deploymentDetailsDto, String releaseResourceId, String releaseVersion) {
        String baseResourcePath = "classpath:/pipeline-templates/sb-standard-dev-pipeline-1.0-tekton-v1beta1/";
        String deploymentAppResourceBasePath = "classpath:/application-templates/spring-boot/";

        Map<String, String> pipelineTemplatingVariables = preparePipelineTemplatingVariables(deploymentDetailsDto, releaseResourceId, releaseVersion);

        List<PipelineResource> resources = new ArrayList<>();

        try {
            PipelineResource pipelinePvc = new PipelineResource();
            pipelinePvc.setFormat("yaml");
            pipelinePvc.setResourceType("pipeline-pvc");

            Map<String, Object> spec = new HashMap<>();
            spec.put("accessModes", new String[]{"ReadWriteOnce"});
            spec.put("volumeMode", "Filesystem");
            spec.put("resources", new SingletonMap("requests", new SingletonMap("storage", pipelineTemplatingVariables.get("pipelinePvcSize"))));

            Map<String, Object> pvcValues = new HashMap<>();
            pvcValues.put("kind", "PersistentVolumeClaim");
            pvcValues.put("apiVersion", "v1");
            pvcValues.put("metadata", new SingletonMap("name", pipelineTemplatingVariables.get("pipelinePvcName")));
            pvcValues.put("spec", spec);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);
            String templatedContent = yaml.dump(pvcValues);
            System.out.println(templatedContent);
            pipelinePvc.setResourceContent(templatedContent);
            resources.add(pipelinePvc);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        try {
            if (!"local".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
                PipelineResource appImagePullSecret = new PipelineResource();
                appImagePullSecret.setFormat("yaml");
                appImagePullSecret.setResourceType("secret");

                Map<String, Object> secretValues = new HashMap<>();
                secretValues.put("kind", "Secret");
                secretValues.put("apiVersion", "v1");
                secretValues.put("type", "kubernetes.io/dockerconfigjson");
                secretValues.put("metadata", new SingletonMap("name", pipelineTemplatingVariables.get("appImagePullSecretName")));
                secretValues.put("data", new SingletonMap(".dockerconfigjson", pipelineTemplatingVariables.get("dockerConfigJson")));

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setPrettyFlow(true);
                Yaml yaml = new Yaml(options);
                String templatedContent = yaml.dump(secretValues);
                System.out.println(templatedContent);
                appImagePullSecret.setResourceContent(templatedContent);
                resources.add(appImagePullSecret);
            }
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

        PipelineResource gitCloneTask = new PipelineResource();
        gitCloneTask.setFormat("yaml");
        gitCloneTask.setResourceType("task");
        try {
            String content = getPipelineTemplateContent(baseResourcePath.concat("task-git.yaml"));
            String templatedContent = getTemplatedPipelineResource(content, pipelineTemplatingVariables);
            gitCloneTask.setResourceContent(templatedContent);
            resources.add(gitCloneTask);
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

        try {
            PipelineResource dockerFileConfigMap = new PipelineResource();
            dockerFileConfigMap.setFormat("yaml");
            dockerFileConfigMap.setResourceType("configmap");

            Map<String, Object> configMapValues = new HashMap<>();
            configMapValues.put("kind", "ConfigMap");
            configMapValues.put("apiVersion", "v1");
            configMapValues.put("metadata", new SingletonMap("name", pipelineTemplatingVariables.get("appDockerFileConfigMapName")));
            String content = getPipelineTemplateContent(deploymentAppResourceBasePath.concat("dockerfile-mvn-template-1"));
            configMapValues.put("data", new SingletonMap("Dockerfile",
                    getTemplatedPipelineResource(content, pipelineTemplatingVariables)));

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(options);
            String templatedContent = yaml.dump(configMapValues);
            dockerFileConfigMap.setResourceContent(templatedContent);
            System.out.println(templatedContent);
            resources.add(dockerFileConfigMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resources;
    }

    public String getPipelineTemplateContent(String templatePath) throws IOException {
        Resource resource = resourceLoader.getResource(templatePath);
        return FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);
    }

    public Map<String, String> preparePipelineTemplatingVariables(DeploymentDetailsDto deploymentDetailsDto, String releaseResourceId, String releaseVersion) {
        String deploymentResourceId = deploymentDetailsDto.getDeploymentId().getDeploymentResourceId();
        Map<String, String> args = new HashMap<>();

        //pipeline pvc
        args.put("pipelinePvcName", "pipeline-pvc-".concat(releaseResourceId));
        args.put("pipelinePvcSize", "1Gi");

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

        //App Image Pull Secret , secured local docker registries are not supported, requires certs.
        String imagePullSecretName = "secret-app-image-pull-".concat(releaseResourceId);
        if (!"local".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            args.put("appImagePullSecretName", imagePullSecretName);
            args.put("dockerConfigJson", getImagePullSecretConfig(deploymentDetailsDto));
        }

        //helm values config map
        args.put("helmValuesConfigMapName", "configmap-helm-values-".concat(releaseResourceId));
        args.put("helmValuesYaml", getHelmValuesYaml(deploymentDetailsDto, releaseVersion, imagePullSecretName));

        //makisu values config map
        args.put("makisuValuesSecretName", "secret-makisu-values-".concat(releaseResourceId));
        args.put("makisuValuesYaml", getMakisuRegistryConfig(deploymentDetailsDto));


        //task git
        args.put("gitCloneTaskName", "task-git-clone-".concat(releaseResourceId));
        //also has "gitRepoUrl", "gitRepoBranchName" which is already added.

        //task helm
        args.put("helmDeployTaskName", "task-helm-deploy-".concat(releaseResourceId));
        //also has helmValuesConfigMapName which is already added.

        //task makisu
        args.put("makisuBuildImageTaskName", "task-makisu-build-".concat(releaseResourceId));
        //also has "gitResourceName" which is already added.

        //pipeline
        args.put("pipelineName", "pipeline-".concat(releaseResourceId));
        args.put("helmReleaseName", getHelmReleaseId(deploymentResourceId));
        args.put("helmCommand", getHelmCommand(releaseVersion));
        args.put("helmChartUrl", "https://ashimusmani.github.io/helm-charts/basic-springboot-demo-ketchup-0.1.0.tgz");
        args.put("containerRegistryUrl", deploymentDetailsDto.getContainerRegistryUrl());

        String imageTag = getImageTagName(deploymentDetailsDto, releaseVersion);
        args.put("imageTag", imageTag);

        args.put("devKubernetesNamespace", deploymentDetailsDto.getDevKubernetesNamespace());
        //also has "gitResourceName", "makisuBuildImageTaskName", "helmDeployTaskName"  which are already added.

        //pipeline run
        args.put("pipelineRunName", "pipeline-run-".concat(releaseResourceId));
        //also has "serviceAccountName", "pipelineName", "gitResourceName", "pipelinePvcName" which are already added.

        if (APP_TYPE_BASIC_SPRING_BOOT.equals(deploymentDetailsDto.getApplicationType())) {
            if (BUILD_TOOL_MAVEN_3.equals(deploymentDetailsDto.getBuildTool())) {
                args.putAll(getMaven3BuildToolDockerFileContent(deploymentDetailsDto));
            } else {
                throw new UnsupportedOperationException("Build tool not supported : " + deploymentDetailsDto.getBuildTool());
            }
        } else {
            throw new UnsupportedOperationException("App type not supported : " + deploymentDetailsDto.getApplicationType());
        }
        //helm values config map
        args.put("appDockerFileConfigMapName", "configmap-app-dockerfile-content-".concat(releaseResourceId));

        return args;
    }

    private Map<String, String> getMaven3BuildToolDockerFileContent(DeploymentDetailsDto deploymentDetails) {
        Map<String, String> args = new HashMap<>();
        args.put("maven.image.name", getMaven3ImageNameForJavaPlatform(deploymentDetails));
        args.put("jre.image.name", getJREImageNameForJavaPlatform(deploymentDetails));
//        args.put("app.jar.name", "ketchup-demo-basicspringboot-0.0.1-SNAPSHOT.jar"); // TODO: 23/08/20 hardcoded
        args.put("app.port", deploymentDetails.getAppServerPort());
        return args;
    }

    private String getJREImageNameForJavaPlatform(DeploymentDetailsDto deploymentDetails) {
        if (isNullOrEmpty(deploymentDetails.getPlatform())) {
            throw new UnexpectedException("Platform cannot be null");
        }
        switch (deploymentDetails.getPlatform()) {
            case PLATFORM_JAVA_8:
                return IMAGE_JRE_JAVA_8;
        }
        throw new UnsupportedOperationException("Platform : " + deploymentDetails.getPlatform() + "not supported");
    }

    private String getMaven3ImageNameForJavaPlatform(DeploymentDetailsDto deploymentDetails) {
        if (isNullOrEmpty(deploymentDetails.getPlatform())) {
            throw new UnexpectedException("Platform cannot be null");
        }
        switch (deploymentDetails.getPlatform()) {
            case PLATFORM_JAVA_8:
                return IMAGE_MAVEN_3_JAVA_8;
        }
        throw new UnsupportedOperationException("Platform : " + deploymentDetails.getPlatform() + "not supported");
    }

    public String getHelmCommand(String releaseVersion) {
        return "upgrade";
    }

    public String getTemplatedPipelineResource(String template, Map<String, String> templatingVariables) {
        StrSubstitutor sub = new StrSubstitutor(templatingVariables, "${", "}");
        String templatedContent = sub.replace(template);
        System.out.println(templatedContent);
        return templatedContent;
    }

    public String getHelmValuesYaml(DeploymentDetailsDto deploymentDetailsDto, String releaseVersion, String imagePullSecretName) {
        LinkedHashMap<String, Object> containerRegistryValues = new LinkedHashMap<>();
        containerRegistryValues.put("repository", getImageTagName(deploymentDetailsDto, releaseVersion));

        LinkedHashMap<String, Object> serviceValues = new LinkedHashMap<>();
        serviceValues.put("type", "ClusterIP");
        serviceValues.put("port", Long.valueOf(deploymentDetailsDto.getAppServerPort()));


        LinkedHashMap<String, Object> helmConfigValues = new LinkedHashMap<>();
        helmConfigValues.put("replicaCount", Long.valueOf(deploymentDetailsDto.getReplicas()));
        helmConfigValues.put("image", containerRegistryValues);
        helmConfigValues.put("service", serviceValues);
        helmConfigValues.put("imagePullSecrets", new SingletonMap[]{new SingletonMap("name", imagePullSecretName)});

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml helmConfigYaml = new Yaml(options);
        String helmConfigString = helmConfigYaml.dump(helmConfigValues);
        System.out.println(helmConfigString);
        return helmConfigString;
    }

    public String getImageTagName(DeploymentDetailsDto deploymentDetailsDto, String releaseVersion) {
        String imageTag = "";
        if ("local".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            if ("".equalsIgnoreCase(deploymentDetailsDto.getContainerRepositoryName())) {
                imageTag = deploymentDetailsDto.getContainerRegistryUrl()
                        + "/" + deploymentDetailsDto.getContainerImageName()
                        + ":" + releaseVersion;
            } else {
                imageTag = deploymentDetailsDto.getContainerRegistryUrl()
                        + "/" + deploymentDetailsDto.getContainerRepositoryName()
                        + "/" + deploymentDetailsDto.getContainerImageName()
                        + ":" + releaseVersion;
            }
        } else if ("docker-hub".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            //docker hub doesnt have different images in repository, repository name and image name should be same.
            imageTag = deploymentDetailsDto.getContainerRegistryUrl()
                    + "/" + deploymentDetailsDto.getContainerRegistryUsername()
                    + "/" + deploymentDetailsDto.getContainerImageName()
                    + ":" + releaseVersion;
        } else if ("gcr".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            //gcr has project id as mandatory part and no nesting is allowed not even single level.
            //repositoryName is project id.
            imageTag = deploymentDetailsDto.getContainerRegistryUrl()
                    + "/" + deploymentDetailsDto.getContainerRepositoryName()
                    + "/" + deploymentDetailsDto.getContainerImageName()
                    + ":" + releaseVersion;
        } else {
            throw new RuntimeException("Unknown registry type supported types are local, docker-hub, aws-ecr, gcr and azurecr.");
        }
        return imageTag;
    }


// local:      "'{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}}}}}}'"
// docker-hub: "'{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}},\"basic\":{\"username\":\"${registryUsername}\",\"password\":\"${registryPassword}\"}}}}}'"
// gcr:        "'{\"${registryUrl}\":{\"ketchup-test/*\":{\"push_chunk\": -1, \"security\":{\"tls\":{\"client\":{\"disabled\":false}},\"basic\":{\"username\":\"${registryUsername}\",\"password\": {${registryPassword}}}}}}}'"

    public String getMakisuRegistryConfig(DeploymentDetailsDto deploymentDetailsDto) {
        if ("local".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            LinkedHashMap<String, Object> containerRegistryValues = new LinkedHashMap<>();
            containerRegistryValues.put("security", new SingletonMap("tls", new SingletonMap("client", new SingletonMap("disabled", false))));

            LinkedHashMap<String, Object> serviceValues = new LinkedHashMap<>();
            serviceValues.put(".*", containerRegistryValues);

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
            serviceValues.put(".*", containerRegistryValues);

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

    @Override
    public Optional<Release> refreshReleaseStatus(String releaseResourceId) {
        return Optional.empty();
    }

    public String getImagePullSecretConfig(DeploymentDetailsDto deploymentDetailsDto) {
        if ("local".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            throw new RuntimeException("UnSupported secured local docker registry, only docker-hub and gcr are currently supported.");
        } else if ("docker-hub".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            String registryUrl = "https://index.docker.io/v1/";
            String userName = deploymentDetailsDto.getContainerRegistryUsername();
            String password = deploymentDetailsDto.getContainerRegistryPassword();
            String auth = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("auths", new JSONObject().put(registryUrl, new JSONObject().put("auth", auth)));
            String encodedConfig = Base64.getEncoder().encodeToString(jsonRequest.toString().getBytes());
            return encodedConfig;
        } else if ("gcr".equalsIgnoreCase(deploymentDetailsDto.getContainerRegistryType())) {
            String registryUrl = "https://gcr.io";
            String userName = deploymentDetailsDto.getContainerRegistryUsername();
            String password = deploymentDetailsDto.getContainerRegistryPassword();
            String auth = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("auths", new JSONObject().put(registryUrl, new JSONObject().put("auth", auth)));
            String encodedConfig = Base64.getEncoder().encodeToString(jsonRequest.toString().getBytes());
            return encodedConfig;
        } else {
            throw new RuntimeException("Unknown registry type supported types are local, docker-hub, aws-ecr, gcr and azurecr.");
        }
    }

    @Override
    public DeploymentDetailsDto extractDeployment(Release release) {
        String deploymentDetailsJSON = release.getDeploymentDataJson();
        try {
            return new ObjectMapper().readValue(deploymentDetailsJSON, DeploymentDetailsDto.class);
        } catch (IOException e) {
            throw new UnexpectedException("Failed while parsing deployment details for release : " + release.getId().getReleaseResourceId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void cleanPipelineResources(ReleaseId releaseId) {
        Set<PipelineResource> resources = pipelineResourceRepository.findDistinctByReleaseResourceId(releaseId.getReleaseResourceId());
        Release release = findById(releaseId.getReleaseResourceId());
        DeploymentDetailsDto deploymentDetailsDto = deploymentService.getDeployment(release.getDeploymentResourceId());
        String kubeConfig = getKubeConfig(release.getDeploymentDataJson());
        deletePipelineResources(deploymentDetailsDto, resources, kubeConfig);
    }

    private String getKubeConfig(String deploymentDataJson) {
        JSONObject jo = new JSONObject(deploymentDataJson);
        return StringUtility.decodeBase64((jo.getString("devKubeconfig")));
    }

    private String getPipelineResourceName(String resourceYaml) {
        Yaml yaml = new Yaml();
        LinkedHashMap<String, Object> pipelineResource = (LinkedHashMap<String, Object>) yaml.loadAs(resourceYaml, Map.class);
        LinkedHashMap<String, Object> metadata = (LinkedHashMap<String, Object>) pipelineResource.get("metadata");
        return (String) metadata.get("name");
    }

    public static String generateForeverActiveToken(AuthorizationServerTokenServices jwtTokenServices, String scope) {
        Map<String, String> authorizationParameters = new HashMap<String, String>();
        authorizationParameters.put("scope", scope);
        authorizationParameters.put("username", "admin@" + AuthUtils.getCurrentTenantId());
        authorizationParameters.put("client_id", "client-id-forever-active");
        authorizationParameters.put("grant", "password");

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));

        Set<String> responseType = new HashSet<String>();
        responseType.add("password");

        Set<String> scopes = new HashSet<String>();
        scopes.add(scope);
//        scopes.add("write");

        OAuth2Request authorizationRequest = new OAuth2Request(
                authorizationParameters, "client-id-forever-active",
                authorities, true, scopes, null, "",
                responseType, null);

        User userPrincipal = new User("admin@" + AuthUtils.getCurrentTenantId(), "", true, true, true, true, authorities);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, authorities);

        OAuth2Authentication authenticationRequest = new OAuth2Authentication(
                authorizationRequest, authenticationToken);
        authenticationRequest.setAuthenticated(true);

        OAuth2AccessToken accessToken = jwtTokenServices.createAccessToken(authenticationRequest);
        return accessToken.toString();
    }

    @Override
    public String generateGitWebhookListenerURL(String vendor, String deploymentResourceId) {
        String domain = ConfigUtility.instance().getProperty("ketchup.base-url");
        String webhookListenerUrl = "v1/release/git-webhook/" + vendor + "/listen?access_token="
                + generateForeverActiveToken(jwtTokenServices, "git-webhook") + "&uid=" + deploymentResourceId;
        System.out.println(webhookListenerUrl);
        return domain + "/" + webhookListenerUrl;
    }
}
