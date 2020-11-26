package org.zigmoi.ketchup.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.zigmoi.ketchup.helm.dtos.ReleaseStatusResponseDto;
import org.zigmoi.ketchup.helm.exceptions.CommandFailureException;
import org.zigmoi.ketchup.project.dtos.settings.BuildToolSettingsResponseDto;
import org.zigmoi.ketchup.project.dtos.settings.ContainerRegistrySettingsResponseDto;
import org.zigmoi.ketchup.project.dtos.settings.KubernetesClusterSettingsResponseDto;
import org.zigmoi.ketchup.project.services.SettingService;
import org.zigmoi.ketchup.application.dtos.ApplicationDetailsDto;
import org.zigmoi.ketchup.exception.UnexpectedException;
import org.zigmoi.ketchup.helm.services.HelmService;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.services.TenantProviderService;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;
import org.zigmoi.ketchup.application.dtos.ApplicationRequestDto;
import org.zigmoi.ketchup.application.dtos.ApplicationResponseDto;
import org.zigmoi.ketchup.application.entities.*;
import org.zigmoi.ketchup.application.repositories.ApplicationRepository;
import org.zigmoi.ketchup.application.repositories.PipelineArtifactRepository;
import org.zigmoi.ketchup.application.repositories.RevisionRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.zigmoi.ketchup.application.ApplicationConstants.*;

@Service
public class ApplicationServiceImpl extends TenantProviderService implements ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static final Map<String, Boolean> tektonConfigAppliedToKubernetesCluster = new ConcurrentHashMap<>();
    private final RevisionRepository revisionRepository;
    private final PipelineArtifactRepository pipelineArtifactRepository;
    private final PermissionUtilsService permissionUtilsService;
    private final ResourceLoader resourceLoader;
    private final AuthorizationServerTokenServices jwtTokenServices;
    private final HelmService helmService;
    private final ApplicationRepository applicationRepository;
    private SettingService settingService;

    @Value("${ketchup.base-url}")
    private String ketchupBaseUrl;

    public ApplicationServiceImpl(RevisionRepository revisionRepository, PipelineArtifactRepository pipelineArtifactRepository, PermissionUtilsService permissionUtilsService, ResourceLoader resourceLoader, AuthorizationServerTokenServices jwtTokenServices, HelmService helmService, ApplicationRepository applicationRepository, SettingService settingService) {
        this.revisionRepository = revisionRepository;
        this.pipelineArtifactRepository = pipelineArtifactRepository;
        this.permissionUtilsService = permissionUtilsService;
        this.resourceLoader = resourceLoader;
        this.jwtTokenServices = jwtTokenServices;
        this.helmService = helmService;
        this.applicationRepository = applicationRepository;
        this.settingService = settingService;
    }

    private static String getNamespaceForTektonPipeline(String devKubernetesClusterSettingId) {
        return "tekton-pipelines";
    }

    private synchronized void checkAndApplyTektonCloudEventURL(ApplicationDetailsDto applicationDetailsDto, Revision revision, String kubeConfig) throws IOException, ApiException {
        if (!(tektonConfigAppliedToKubernetesCluster.containsKey(applicationDetailsDto.getDevKubernetesClusterSettingId())
                && tektonConfigAppliedToKubernetesCluster.get(applicationDetailsDto.getDevKubernetesClusterSettingId()))) {
            String templatedContent = "";
            try {
                PipelineArtifact tektonCloudEventSinkConfig = new PipelineArtifact();
                tektonCloudEventSinkConfig.setFormat("yaml");
                tektonCloudEventSinkConfig.setResourceType("configmap");

                Map<String, Object> labels = new HashMap<>();
                labels.put("app.kubernetes.io/instance", "default");
                labels.put("app.kubernetes.io/part-of", "tekton-pipelines");

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("name", "config-defaults");
                metadata.put("namespace", getNamespaceForTektonPipeline(applicationDetailsDto.getDevKubernetesClusterSettingId()));
                metadata.put("labels", labels);

                Map<String, Object> configMapValues = new HashMap<>();
                configMapValues.put("kind", "ConfigMap");
                configMapValues.put("apiVersion", "v1");
                configMapValues.put("metadata", metadata);
                configMapValues.put("data", new SingletonMap("default-cloud-events-sink", buildTektonCloudEventSinkURL(revision)));

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setPrettyFlow(true);
                Yaml yaml = new Yaml(options);
                templatedContent = yaml.dump(configMapValues);
                System.out.println(templatedContent);
                tektonCloudEventSinkConfig.setResourceContent(templatedContent);
                KubernetesUtility.createConfigmapUsingYamlContent(templatedContent,
                        getNamespaceForTektonPipeline(applicationDetailsDto.getDevKubernetesClusterSettingId()), "false", kubeConfig);
            } catch (ApiException e) {
                if (e.getMessage().toLowerCase().contains("conflict")) {
                    KubernetesUtility.updateConfigmapUsingYamlContent("config-defaults",
                            getNamespaceForTektonPipeline(applicationDetailsDto.getDevKubernetesClusterSettingId()), templatedContent, kubeConfig);
                }
            } catch (IOException e) {
                throw new UnexpectedException(e.getMessage(), e);
            }

            tektonConfigAppliedToKubernetesCluster.put(applicationDetailsDto.getDevKubernetesClusterSettingId(), true);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#applicationId.projectResourceId)")
    public String createRevision(String trigger, String commitId, ApplicationId applicationId) {
        //validate and fetch applicationId.
        //get projectId from application.
        //handle duplicate pipeline resources error in k8s.
        ApplicationDetailsDto applicationDetailsDto = getApplication(applicationId);
        String revisionVersion = getNextRevisionVersion(applicationId.getApplicationResourceId());

        Revision r = new Revision();
        String revisionResourceId = UUID.randomUUID().toString();
        RevisionId revisionId = new RevisionId(applicationId, revisionResourceId);
        r.setId(revisionId);
        r.setHelmReleaseId(getHelmReleaseId(applicationId.getApplicationResourceId()));
        r.setVersion(revisionVersion);
        r.setRollback(false);
        r.setDeploymentTriggerType(trigger);

        if (DeploymentTriggerType.GIT_WEBHOOK.toString().equalsIgnoreCase(trigger)) {
            if (StringUtility.isNullOrEmpty(commitId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GIT WEBHOOK initiated deployments should have valid commit id.");
            } else {
                r.setCommitId(commitId);
            }
        } else if (DeploymentTriggerType.MANUAL.toString().equalsIgnoreCase(trigger)) {
            if (StringUtility.isNullOrEmpty(commitId)) {
                r.setCommitId("");
            } else {
                r.setCommitId(commitId);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown deployment trigger type., " + trigger);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            r.setApplicationDataJson(objectMapper.writeValueAsString(applicationDetailsDto));
        } catch (
                JsonProcessingException e) {
            e.printStackTrace();
        }

        //generate pipeline all resources and save them, name can be parsed from them directly.
        List<PipelineArtifact> pipelineArtifacts = generatePipelineResources_tekton_v1beta1("sb-standard-dev-1.0",
                applicationDetailsDto, revisionResourceId, revisionVersion, commitId);
        pipelineArtifacts.forEach(pipelineArtifact ->

        {
            PipelineArtifactId pipelineArtifactId = new PipelineArtifactId(revisionId, UUID.randomUUID().toString());
            pipelineArtifact.setId(pipelineArtifactId);
        });
        pipelineArtifactRepository.saveAll(pipelineArtifacts);
        revisionRepository.saveAndFlush(r);

        String kubeConfig = StringUtility.decodeBase64(applicationDetailsDto.getDevKubeconfig());

        queueAndDeployPipelineResources(pipelineArtifacts, kubeConfig, applicationDetailsDto, r);
        return revisionResourceId;
    }

    private String getNextRevisionVersion(String applicationResourceId) {
        long noOfRevisions = revisionRepository.countAllByApplicationResourceId(applicationResourceId);
        String revisionVersion = "v".concat(String.valueOf(noOfRevisions + 1));
        return revisionVersion;
    }

    private void queueAndDeployPipelineResources(List<PipelineArtifact> pipelineArtifacts, String kubeConfig,
                                                 ApplicationDetailsDto applicationDetailsDto, Revision r) {
        deployPipelineResources(pipelineArtifacts, kubeConfig, applicationDetailsDto, r);
    }

    private String getHelmReleaseId(String applicationResourceId) {
        return "app-".concat(applicationResourceId);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#revisionId.projectResourceId)")
    public void rollbackToRevision(RevisionId revisionId) {
        Revision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Revision with id %s not found.", revisionId.getRevisionResourceId())));
        ApplicationId applicationId = new ApplicationId(revisionId.getTenantId(), revisionId.getProjectResourceId(), revisionId.getApplicationResourceId());
        ApplicationDetailsDto applicationDetails = getApplication(applicationId);
        String namespace = applicationDetails.getDevKubernetesNamespace();
        String kubeConfig = StringUtility.decodeBase64(applicationDetails.getDevKubeconfig());
        String helmReleaseName = revision.getHelmReleaseId();
        String helmReleaseVersionNumber = revision.getHelmReleaseVersion();
        if (isNullOrEmpty(helmReleaseVersionNumber)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operation failed, did not find required helm release version to rollback to.");
        }
        Revision clonedRevision = cloneAndSaveRevision(revision);
        try {
            helmService.rollbackRelease(helmReleaseName, helmReleaseVersionNumber, namespace, kubeConfig);
            ReleaseStatusResponseDto releaseStatus = helmService.getReleaseStatus(getHelmReleaseId(revisionId.getApplicationResourceId()), namespace, kubeConfig);
            String latestReleaseVersion = String.valueOf(releaseStatus.getVersion());
            clonedRevision.setHelmReleaseVersion(latestReleaseVersion);
            clonedRevision.setStatus("SUCCESS");
            revisionRepository.save(clonedRevision);
        } catch (Exception e) {
            clonedRevision.setStatus("FAILED");
            revisionRepository.save(clonedRevision);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operation failed.");
        }
    }

    private Revision cloneAndSaveRevision(Revision revision) {
        Revision clonedRevision = new Revision();
        RevisionId id = new RevisionId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setProjectResourceId(revision.getId().getProjectResourceId());
        id.setApplicationResourceId(revision.getId().getApplicationResourceId());
        id.setRevisionResourceId(UUID.randomUUID().toString());

        clonedRevision.setId(id);
        clonedRevision.setVersion(getNextRevisionVersion(revision.getId().getApplicationResourceId()));
        clonedRevision.setStatus("IN PROGRESS");
        clonedRevision.setErrorMessage(null);
        clonedRevision.setPipelineStatusJson(null);
        clonedRevision.setCommitId(revision.getCommitId());
        clonedRevision.setHelmChartId(revision.getHelmChartId());
        clonedRevision.setHelmReleaseId(getHelmReleaseId(revision.getId().getApplicationResourceId()));
        clonedRevision.setRollback(true);
        clonedRevision.setHelmReleaseVersion(null);
        clonedRevision.setOriginalRevisionVersionId(revision.getVersion());
        clonedRevision.setApplicationDataJson(revision.getApplicationDataJson());
        revisionRepository.saveAndFlush(clonedRevision);
        return clonedRevision;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#revisionId.projectResourceId)")
    public void stopRevisionPipeline(RevisionId revisionId) {
        Revision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Revision with id %s not found.", revisionId.getRevisionResourceId())));

        ApplicationId applicationId = new ApplicationId(revisionId.getTenantId(), revisionId.getProjectResourceId(), revisionId.getApplicationResourceId());
        ApplicationDetailsDto applicationDetails = getApplication(applicationId);
        String namespace = applicationDetails.getDevKubernetesNamespace();
        //check if revision is not failed/success than cancel the pipeline.
        PipelineArtifact resource = pipelineArtifactRepository.findByRevisionResourceIdAndResourceType(revisionId.getRevisionResourceId(), "pipeline-run").orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                String.format("Pipeline run for revision with id %s not found.", revisionId.getRevisionResourceId())));

        Yaml yaml = new Yaml();
        LinkedHashMap<String, Object> pipelineRun = (LinkedHashMap<String, Object>) yaml.loadAs(resource.getResourceContent(), Map.class);
        LinkedHashMap<String, Object> metadata = (LinkedHashMap<String, Object>) pipelineRun.get("metadata");
        String resourceName = (String) metadata.get("name");

        ObjectMapper objectMapper = new ObjectMapper();
        ApplicationDetailsDto applicationDetailsDto = null;
        try {
            applicationDetailsDto = objectMapper.readValue(revision.getApplicationDataJson(), ApplicationDetailsDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String kubeConfig = StringUtility.decodeBase64(applicationDetailsDto.getDevKubeconfig());
        try {
            String jsonPatchContent =
                    "[{\"op\":\"add\",\"path\":\"/spec/status\",\"value\":\"PipelineRunCancelled\"}]";
            KubernetesUtility.patchCRDUsingYamlContent(resourceName, jsonPatchContent, namespace, "tekton.dev", "v1beta1", "pipelineruns", kubeConfig);
        } catch (IOException | ApiException e) {
            logger.error("Failed to cancel pipeline, ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cancel pipeline.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#revisionId.projectResourceId)")
    public Revision findRevisionById(RevisionId revisionId) {
        return revisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Revision with id %s not found.", revisionId.getRevisionResourceId())));
    }

    @Override
    @Transactional(readOnly = true)
    @PostAuthorize("@permissionUtilsService.canPrincipalReadApplication(returnObject.id.projectResourceId)")
    public Revision findRevisionByResourceId(String revisionResourceId) {
        return revisionRepository.findByRevisionResourceId(revisionResourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Revision with id %s not found.", revisionResourceId)));
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteApplication(#revisionId.projectResourceId)")
    public void deleteRevision(RevisionId revisionId) {
        if (revisionRepository.existsById(revisionId) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Revision with id %s not found.", revisionId.getRevisionResourceId()));
        }
        revisionRepository.deleteById(revisionId);
        pipelineArtifactRepository.deleteAllByRevisionResourceId(revisionId.getRevisionResourceId());
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteApplication(#applicationId.projectResourceId)")
    public void deleteApplication(ApplicationId applicationId) {
        Optional<Revision> activeRevision = getActiveRevision(applicationId);
        if (activeRevision.isPresent()) {
            ApplicationDetailsDto applicationDetailsDto = applicationJsonToDto(activeRevision.get().getApplicationDataJson());
            String namespace = applicationDetailsDto.getDevKubernetesNamespace();
            String kubeConfig = StringUtility.decodeBase64(applicationDetailsDto.getDevKubeconfig());
            try {
                helmService.uninstallChart("app-" + applicationId.getApplicationResourceId(), namespace, kubeConfig);
            } catch (CommandFailureException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operation failed.");
            }
        } else {
            System.out.println("No active revision found to uninstall.");
        }

        List<Revision> revisions = revisionRepository.findAllByApplicationResourceId(applicationId.getApplicationResourceId());
        revisions.forEach(revision -> pipelineArtifactRepository.deleteAllByRevisionResourceId(revision.getId().getRevisionResourceId()));
        revisionRepository.deleteAllByApplicationResourceId(applicationId.getApplicationResourceId());
        applicationRepository.deleteById(applicationId);
    }

    public ApplicationDetailsDto applicationJsonToDto(String applicationJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        ApplicationDetailsDto applicationDetailsDto = null;
        try {
            applicationDetailsDto = objectMapper.readValue(applicationJson, ApplicationDetailsDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return applicationDetailsDto;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#revision.id.projectResourceId)")
    public void updateRevision(Revision revision) {

    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#applicationId.projectResourceId)")
    public Optional<Revision> getActiveRevision(ApplicationId applicationId) {
        String applicationResourceId = applicationId.getApplicationResourceId();
        List<Revision> revisions = revisionRepository.findActiveRevision(applicationResourceId, "SUCCESS", PageRequest.of(0, 1));
        return revisions.stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#applicationId.projectResourceId)")
    public Set<Revision> listAllRevisionsInApplication(ApplicationId applicationId) {
        return revisionRepository.findDistinctByApplicationResourceIdOrderByCreatedOnDesc(applicationId.getApplicationResourceId());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public Set<Revision> listAllRevisionsInProject(String projectResourceId) {
        return revisionRepository.findDistinctByProjectResourceId(projectResourceId);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public List<Revision> listRecentRevisionPipelinesInProject(String projectResourceId) {
        return revisionRepository.listRecentRevisionPipelinesInProject(projectResourceId, PageRequest.of(0, 5));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public Set<Revision> listAllRevisionsInProjectWithStatus(String projectResourceId, String status) {
        return revisionRepository.findDistinctByProjectResourceIdAndStatusOrderByLastUpdatedOnDesc(projectResourceId, status);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#revisionId.projectResourceId)")
    public Set<PipelineArtifact> listAllPipelineArtifactsInRevision(RevisionId revisionId) {
        if (revisionRepository.existsById(revisionId) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Revision with id %s not found.", revisionId.getRevisionResourceId()));
        }
        return pipelineArtifactRepository.findDistinctByRevisionResourceId(revisionId.getRevisionResourceId());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#pipelineArtifactId.projectResourceId)")
    public PipelineArtifact getPipelineArtifactsById(PipelineArtifactId pipelineArtifactId) {
        return pipelineArtifactRepository
                .findById(pipelineArtifactId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format("Pipeline Resource with id %s not found.", pipelineArtifactId.getPipelineArtifactResourceId())));
    }

    private void deployPipelineResources(List<PipelineArtifact> pipelineArtifacts, String kubeConfig, ApplicationDetailsDto applicationDetailsDto, Revision revision) {
        try {
            checkAndApplyTektonCloudEventURL(applicationDetailsDto, revision, kubeConfig);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        String namespace = applicationDetailsDto.getDevKubernetesNamespace();
        pipelineArtifacts.stream()
                .filter(r -> "pipeline-pvc".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineArtifact -> {
                    try {
                        KubernetesUtility.createPvcUsingYamlContent(pipelineArtifact.getResourceContent(), namespace, "false", kubeConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        pipelineArtifacts.stream()
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
        pipelineArtifacts.stream()
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

        pipelineArtifacts.stream()
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

        pipelineArtifacts.stream()
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

        pipelineArtifacts.stream()
                .filter(r -> "pipeline".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipeline -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(pipeline.getResourceContent(), namespace, "tekton.dev", "v1beta1", "pipelines", "false", kubeConfig);
                    } catch (IOException | ApiException e) {
                        e.printStackTrace();
                    }
                });

        //check pipeline-run is not more than one.
        pipelineArtifacts.stream()
                .filter(r -> "pipeline-run".equalsIgnoreCase(r.getResourceType()))
                .forEach(pipelineRun -> {
                    try {
                        KubernetesUtility.createCRDUsingYamlContent(pipelineRun.getResourceContent(), namespace, "tekton.dev", "v1beta1", "pipelineruns", "false", kubeConfig);
                    } catch (IOException | ApiException e) {
                        e.printStackTrace();
                    }
                });
    }

    private String buildTektonCloudEventSinkURL(Revision revision) {
        String domain = ketchupBaseUrl;
        String tektonEventSink = ConfigUtility.instance().getProperty("ketchup.tekton-event-sink-api-path");
        String accessToken = generateForeverActiveToken(jwtTokenServices, "tekton-event");
        return domain + "/" + tektonEventSink + "?access_token=" + accessToken;
    }

    private void deletePipelineResources(ApplicationDetailsDto applicationDetailsDto, Collection<PipelineArtifact> pipelineArtifacts, String kubeConfig) {

        String namespace = applicationDetailsDto.getDevKubernetesNamespace();
        pipelineArtifacts.stream()
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

        pipelineArtifacts.stream()
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

        pipelineArtifacts.stream()
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


        pipelineArtifacts.stream()
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
        pipelineArtifacts.stream()
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

        pipelineArtifacts.stream()
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

        pipelineArtifacts.stream()
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

    private List<PipelineArtifact> generatePipelineResources_tekton_v1beta1(String pipelineType,
                                                                            ApplicationDetailsDto applicationDetailsDto,
                                                                            String revisionResourceId,
                                                                            String revisionVersion,
                                                                            String commitId) {
        String baseResourcePath = "classpath:/pipeline-templates/sb-standard-dev-pipeline-1.0-tekton-v1beta1/";
        String deploymentAppResourceBasePath = "classpath:/application-templates/spring-boot/";

        Map<String, String> pipelineTemplatingVariables = preparePipelineTemplatingVariables(applicationDetailsDto, revisionResourceId, revisionVersion, commitId);

        List<PipelineArtifact> resources = new ArrayList<>();

        try {
            PipelineArtifact pipelinePvc = new PipelineArtifact();
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
            PipelineArtifact helmValuesConfigMap = new PipelineArtifact();
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
            PipelineArtifact makisuRegistrySecret = new PipelineArtifact();
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
            if (!"local".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
                PipelineArtifact appImagePullSecret = new PipelineArtifact();
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

        PipelineArtifact gitSecret = new PipelineArtifact();
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

        PipelineArtifact serviceAccount = new PipelineArtifact();
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

        PipelineArtifact kubeconfigSecret = new PipelineArtifact();
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

        PipelineArtifact gitCloneTask = new PipelineArtifact();
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


        PipelineArtifact makisuBuildTask = new PipelineArtifact();
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

        PipelineArtifact helmDeployTask = new PipelineArtifact();
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

        PipelineArtifact tknPipeline = new PipelineArtifact();
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

        PipelineArtifact tknPipelineRun = new PipelineArtifact();
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
            PipelineArtifact dockerFileConfigMap = new PipelineArtifact();
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

    public Map<String, String> preparePipelineTemplatingVariables(ApplicationDetailsDto applicationDetailsDto, String revisionResourceId, String revisionVersion, String commitId) {
        String applicationResourceId = applicationDetailsDto.getApplicationId().getApplicationResourceId();
        Map<String, String> args = new HashMap<>();

        //pipeline pvc
        args.put("pipelinePvcName", "pipeline-pvc-".concat(revisionResourceId));
        args.put("pipelinePvcSize", "1Gi");

        //git secret
        args.put("gitRepoSecretName", "git-secret-".concat(revisionResourceId));
        args.put("gitRepoBaseUrl", applicationDetailsDto.getGitRepoUrl());
        args.put("gitRepoUsername", applicationDetailsDto.getGitRepoUsername());
        args.put("gitRepoPassword", applicationDetailsDto.getGitRepoPassword());

        //kubeconfig secret
        args.put("kubeConfigSecretName", "kubeconfig-secret-".concat(revisionResourceId));
        args.put("kubeConfigBase64", applicationDetailsDto.getDevKubeconfig());

        //service account
        args.put("serviceAccountName", "service-account-".concat(revisionResourceId));
        //also has "gitRepoSecretName" which is already added.

        //App Image Pull Secret , secured local docker registries are not supported, requires certs.
        String imagePullSecretName = "secret-app-image-pull-".concat(revisionResourceId);
        if (!"local".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            args.put("appImagePullSecretName", imagePullSecretName);
            args.put("dockerConfigJson", getImagePullSecretConfig(applicationDetailsDto));
        }

        //helm values config map
        args.put("helmValuesConfigMapName", "configmap-helm-values-".concat(revisionResourceId));
        args.put("helmValuesYaml", getHelmValuesYaml(applicationDetailsDto, revisionVersion, imagePullSecretName));

        //makisu values config map
        args.put("makisuValuesSecretName", "secret-makisu-values-".concat(revisionResourceId));
        args.put("makisuValuesYaml", getMakisuRegistryConfig(applicationDetailsDto));


        //task git
        args.put("gitCloneTaskName", "task-git-clone-".concat(revisionResourceId));
        args.put("gitResourceName", "git-resource-".concat(revisionResourceId));
        args.put("gitRepoUrl", applicationDetailsDto.getGitRepoUrl());
        if(StringUtility.isNullOrEmpty(commitId)){
            args.put("gitRevision", applicationDetailsDto.getGitRepoBranchName());
        }else{
            args.put("gitRevision", commitId);
        }

        //task helm
        args.put("helmDeployTaskName", "task-helm-deploy-".concat(revisionResourceId));
        //also has helmValuesConfigMapName which is already added.

        //task makisu
        args.put("makisuBuildImageTaskName", "task-makisu-build-".concat(revisionResourceId));
        //also has "gitResourceName" which is already added.

        //pipeline
        args.put("pipelineName", "pipeline-".concat(revisionResourceId));
        args.put("helmReleaseName", getHelmReleaseId(applicationResourceId));
        args.put("helmCommand", getHelmCommand());
        args.put("helmChartUrl", "https://ashimusmani.github.io/helm-charts/basic-springboot-demo-ketchup-0.1.0.tgz");
        args.put("containerRegistryUrl", applicationDetailsDto.getContainerRegistryUrl());

        String imageTag = getImageTagName(applicationDetailsDto, revisionVersion);
        args.put("imageTag", imageTag);

        args.put("devKubernetesNamespace", applicationDetailsDto.getDevKubernetesNamespace());
        //also has "gitResourceName", "makisuBuildImageTaskName", "helmDeployTaskName"  which are already added.

        //pipeline run
        args.put("pipelineRunName", "pipeline-run-".concat(revisionResourceId));
        //also has "serviceAccountName", "pipelineName", "gitResourceName", "pipelinePvcName" which are already added.

        if (APP_TYPE_WEB_APPLICATION.equals(applicationDetailsDto.getApplicationType())) {
            if (BUILD_TOOL_MAVEN_3.equals(applicationDetailsDto.getBuildTool())) {
                args.putAll(getMaven3BuildToolDockerFileContent(applicationDetailsDto));
            } else {
                throw new UnsupportedOperationException("Build tool not supported : " + applicationDetailsDto.getBuildTool());
            }
        } else {
            throw new UnsupportedOperationException("App type not supported : " + applicationDetailsDto.getApplicationType());
        }
        //helm values config map
        args.put("appDockerFileConfigMapName", "configmap-app-dockerfile-content-".concat(revisionResourceId));

        return args;
    }

    private Map<String, String> getMaven3BuildToolDockerFileContent(ApplicationDetailsDto applicationDetails) {
        Map<String, String> args = new HashMap<>();
        args.put("maven.image.name", getMaven3ImageNameForJavaPlatform(applicationDetails));
        args.put("jre.image.name", getJREImageNameForJavaPlatform(applicationDetails));
//        args.put("app.jar.name", "ketchup-demo-basicspringboot-0.0.1-SNAPSHOT.jar"); // TODO: 23/08/20 hardcoded
        args.put("app.port", applicationDetails.getAppServerPort());
        return args;
    }

    private String getJREImageNameForJavaPlatform(ApplicationDetailsDto applicationDetails) {
        if (isNullOrEmpty(applicationDetails.getPlatform())) {
            throw new UnexpectedException("Platform cannot be null");
        }
        switch (applicationDetails.getPlatform()) {
            case PLATFORM_JAVA_8:
                return IMAGE_JRE_JAVA_8;
        }
        throw new UnsupportedOperationException("Platform : " + applicationDetails.getPlatform() + "not supported");
    }

    private String getMaven3ImageNameForJavaPlatform(ApplicationDetailsDto applicationDetails) {
        if (isNullOrEmpty(applicationDetails.getPlatform())) {
            throw new UnexpectedException("Platform cannot be null");
        }
        switch (applicationDetails.getPlatform()) {
            case PLATFORM_JAVA_8:
                return IMAGE_MAVEN_3_JAVA_8;
        }
        throw new UnsupportedOperationException("Platform : " + applicationDetails.getPlatform() + "not supported");
    }

    public String getHelmCommand() {
        return "upgrade";
    }

    public String getTemplatedPipelineResource(String template, Map<String, String> templatingVariables) {
        StrSubstitutor sub = new StrSubstitutor(templatingVariables, "${", "}");
        String templatedContent = sub.replace(template);
        System.out.println(templatedContent);
        return templatedContent;
    }

    public String getHelmValuesYaml(ApplicationDetailsDto applicationDetailsDto, String revisionVersion, String imagePullSecretName) {
        LinkedHashMap<String, Object> containerRegistryValues = new LinkedHashMap<>();
        containerRegistryValues.put("repository", getImageTagName(applicationDetailsDto, revisionVersion));

        LinkedHashMap<String, Object> serviceValues = new LinkedHashMap<>();
        serviceValues.put("type", "ClusterIP");
        serviceValues.put("port", Long.valueOf(applicationDetailsDto.getAppServerPort()));

        LinkedHashMap<String, Object> helmConfigValues = new LinkedHashMap<>();
        helmConfigValues.put("replicaCount", Long.valueOf(applicationDetailsDto.getReplicas()));
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

    public String getImageTagName(ApplicationDetailsDto applicationDetailsDto, String revisionVersion) {
        String imageTag = "";
        if ("local".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            if ("".equalsIgnoreCase(applicationDetailsDto.getContainerRepositoryName())) {
                imageTag = applicationDetailsDto.getContainerRegistryUrl()
                        + "/" + applicationDetailsDto.getContainerImageName()
                        + ":" + revisionVersion;
            } else {
                imageTag = applicationDetailsDto.getContainerRegistryUrl()
                        + "/" + applicationDetailsDto.getContainerRepositoryName()
                        + "/" + applicationDetailsDto.getContainerImageName()
                        + ":" + revisionVersion;
            }
        } else if ("docker-hub".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            //docker hub doesnt have different images in repository, repository name and image name should be same.
            imageTag = applicationDetailsDto.getContainerRegistryUrl()
                    + "/" + applicationDetailsDto.getContainerRegistryUsername()
                    + "/" + applicationDetailsDto.getContainerImageName()
                    + ":" + revisionVersion;
        } else if ("gcr".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            //gcr has project id as mandatory part and no nesting is allowed not even single level.
            //repositoryName is project id.
            imageTag = applicationDetailsDto.getContainerRegistryUrl()
                    + "/" + applicationDetailsDto.getContainerRepositoryName()
                    + "/" + applicationDetailsDto.getContainerImageName()
                    + ":" + revisionVersion;
        } else {
            throw new RuntimeException("Unknown registry type supported types are local, docker-hub, aws-ecr, gcr and azurecr.");
        }
        return imageTag;
    }


// local:      "'{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}}}}}}'"
// docker-hub: "'{\"${registryUrl}\":{\".*\":{\"security\":{\"tls\":{\"client\":{\"disabled\":false}},\"basic\":{\"username\":\"${registryUsername}\",\"password\":\"${registryPassword}\"}}}}}'"
// gcr:        "'{\"${registryUrl}\":{\"ketchup-test/*\":{\"push_chunk\": -1, \"security\":{\"tls\":{\"client\":{\"disabled\":false}},\"basic\":{\"username\":\"${registryUsername}\",\"password\": {${registryPassword}}}}}}}'"

    public String getMakisuRegistryConfig(ApplicationDetailsDto applicationDetailsDto) {
        if ("local".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            LinkedHashMap<String, Object> containerRegistryValues = new LinkedHashMap<>();
            containerRegistryValues.put("security", new SingletonMap("tls", new SingletonMap("client", new SingletonMap("disabled", false))));

            LinkedHashMap<String, Object> serviceValues = new LinkedHashMap<>();
            serviceValues.put(".*", containerRegistryValues);

            String registryUrl = applicationDetailsDto.getContainerRegistryUrl();
            SingletonMap conf = new SingletonMap(registryUrl, serviceValues);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml makisuRegistryConfigYaml = new Yaml(options);
            String makisuRegistryConfigString = makisuRegistryConfigYaml.dump(conf);
            System.out.println(makisuRegistryConfigString);
            String encodedConfig = Base64.getEncoder().encodeToString(makisuRegistryConfigString.getBytes());
            return encodedConfig;
        } else if ("docker-hub".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            //docker hub doesnt have different images in repository, repository name and image name should be same.
            LinkedHashMap<String, Object> creds = new LinkedHashMap<>();
            creds.put("username", applicationDetailsDto.getContainerRegistryUsername());
            creds.put("password", applicationDetailsDto.getContainerRegistryPassword());

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
        } else if ("gcr".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            //gcr has project id as mandatory part and no nesting is allowed not even single level.
            LinkedHashMap<String, Object> creds = new LinkedHashMap<>();
            creds.put("username", applicationDetailsDto.getContainerRegistryUsername());
            creds.put("password", applicationDetailsDto.getContainerRegistryPassword());

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
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#revisionId.projectResourceId)")
    public Optional<Revision> refreshRevisionStatus(RevisionId revisionId) {
        return Optional.empty();
    }

    public String getImagePullSecretConfig(ApplicationDetailsDto applicationDetailsDto) {
        if ("local".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            throw new RuntimeException("UnSupported secured local docker registry, only docker-hub and gcr are currently supported.");
        } else if ("docker-hub".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            String registryUrl = "https://index.docker.io/v1/";
            String userName = applicationDetailsDto.getContainerRegistryUsername();
            String password = applicationDetailsDto.getContainerRegistryPassword();
            String auth = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("auths", new JSONObject().put(registryUrl, new JSONObject().put("auth", auth)));
            String encodedConfig = Base64.getEncoder().encodeToString(jsonRequest.toString().getBytes());
            return encodedConfig;
        } else if ("gcr".equalsIgnoreCase(applicationDetailsDto.getContainerRegistryType())) {
            String registryUrl = "https://gcr.io";
            String userName = applicationDetailsDto.getContainerRegistryUsername();
            String password = applicationDetailsDto.getContainerRegistryPassword();
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
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#revision.id.projectResourceId)")
    public ApplicationDetailsDto extractApplicationByRevisionId(Revision revision) {
        String applicationDetailsJSON = revision.getApplicationDataJson();
        try {
            return new ObjectMapper().readValue(applicationDetailsJSON, ApplicationDetailsDto.class);
        } catch (IOException e) {
            throw new UnexpectedException("Failed while parsing application details for revision : " + revision.getId().getRevisionResourceId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteApplication(#revisionId.projectResourceId)")
    public void cleanPipelineResourcesInRevision(RevisionId revisionId) {
        Set<PipelineArtifact> resources = pipelineArtifactRepository.findDistinctByRevisionResourceId(revisionId.getRevisionResourceId());
        Revision revision = findRevisionById(revisionId);
        ApplicationId applicationId = new ApplicationId(revisionId.getTenantId(), revisionId.getProjectResourceId(), revisionId.getApplicationResourceId());
        ApplicationDetailsDto applicationDetailsDto = getApplication(applicationId);
        String kubeConfig = getKubeConfig(revision.getApplicationDataJson());
        deletePipelineResources(applicationDetailsDto, resources, kubeConfig);
    }

    private String getKubeConfig(String applicationDataJson) {
        JSONObject jo = new JSONObject(applicationDataJson);
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
    public String generateGitWebhookListenerURL(String vendor, ApplicationId applicationId) {
        String domain = ketchupBaseUrl;
        String webhookListenerUrl = "v1-alpha/projects/"
                + applicationId.getProjectResourceId()
                + "/applications/"
                + applicationId.getApplicationResourceId()
                + "/git-webhook/listen?vendor="
                + vendor
                + "&access_token="
                + generateForeverActiveToken(jwtTokenServices, "git-webhook");
        System.out.println(webhookListenerUrl);
        return domain + "/" + webhookListenerUrl;
    }


    private String getNewApplicationId() {
        return UUID.randomUUID().toString();
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#projectResourceId)")
    public String createApplication(String projectResourceId, ApplicationRequestDto applicationRequestDto) {
        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, getNewApplicationId());
        Application application = new Application();
        application.setId(applicationId);
        application.setType(applicationRequestDto.getApplicationType());
        application.setDisplayName(applicationRequestDto.getDisplayName());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JSONObject applicationJson = new JSONObject(objectMapper.writeValueAsString(applicationRequestDto));
            JSONObject applicationIdJson = new JSONObject(objectMapper.writeValueAsString(applicationId));
            applicationJson.put("applicationId", applicationIdJson);

            //get all setting values and store it in application.
            final KubernetesClusterSettingsResponseDto devKubernetesCluster = settingService.getKubernetesCluster(projectResourceId, applicationRequestDto.getDevKubernetesClusterSettingId());
            final ContainerRegistrySettingsResponseDto containerRegistry = settingService.getContainerRegistry(projectResourceId, applicationRequestDto.getContainerRegistrySettingId());
            //save settings for host alias settings

            applicationJson.put("devKubeconfig", devKubernetesCluster.getKubeconfig());
            applicationJson.put("containerRegistryType", containerRegistry.getType());
            applicationJson.put("containerRegistryUrl", containerRegistry.getRegistryUrl());
            applicationJson.put("containerRegistryUsername", containerRegistry.getRegistryUsername());
            applicationJson.put("containerRegistryPassword", containerRegistry.getRegistryPassword());
            applicationJson.put("containerRepositoryName", containerRegistry.getRepository());

            String buildToolSettingId = applicationRequestDto.getBuildToolSettingId();
            if (buildToolSettingId != null && buildToolSettingId != "") {
                final BuildToolSettingsResponseDto buildTool = settingService.getBuildTool(projectResourceId, applicationRequestDto.getBuildToolSettingId());
                applicationJson.put("buildToolType", buildTool.getType());
                applicationJson.put("buildToolSettingsData", buildTool.getFileData());
            }

            application.setData(applicationJson.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        applicationRepository.save(application);
        return applicationId.getApplicationResourceId();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#applicationId.projectResourceId)")
    public ApplicationDetailsDto getApplication(ApplicationId applicationId) {
        Application application = applicationRepository.getByApplicationResourceId(applicationId.getApplicationResourceId());
        ObjectMapper objectMapper = new ObjectMapper();
        ApplicationDetailsDto applicationDetailsDto = null;
        try {
            applicationDetailsDto = objectMapper.readValue(application.getData(), ApplicationDetailsDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return applicationDetailsDto;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#applicationId.projectResourceId)")
    public ApplicationResponseDto getApplicationDetails(ApplicationId applicationId) {
        Application application = applicationRepository.getByApplicationResourceId(applicationId.getApplicationResourceId());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ApplicationResponseDto applicationResponseDto = null;
        try {
            applicationResponseDto = objectMapper.readValue(application.getData(), ApplicationResponseDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return applicationResponseDto;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public List<Application> listAllApplicationsInProject(String projectResourceId) {
        return applicationRepository.findAll()
                .stream()
                .filter(application ->
                        application.getId().getProjectResourceId().equalsIgnoreCase(projectResourceId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#projectResourceId)")
    public void updateApplication(String projectResourceId, String applicationResourceId, ApplicationRequestDto applicationRequestDto) {
        Application application = applicationRepository.getByApplicationResourceId(applicationResourceId);
        ApplicationId applicationId = application.getId();
        application.setDisplayName(applicationRequestDto.getDisplayName());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JSONObject applicationJson = new JSONObject(objectMapper.writeValueAsString(applicationRequestDto));
            JSONObject applicationIdJson = new JSONObject(objectMapper.writeValueAsString(applicationId));
            applicationJson.put("applicationId", applicationIdJson);

            //get all setting values and store it in application.
            final KubernetesClusterSettingsResponseDto devKubernetesCluster = settingService.getKubernetesCluster(projectResourceId, applicationRequestDto.getDevKubernetesClusterSettingId());
            final ContainerRegistrySettingsResponseDto containerRegistry = settingService.getContainerRegistry(projectResourceId, applicationRequestDto.getContainerRegistrySettingId());
            //save settings for host alias settings

            applicationJson.put("devKubeconfig", devKubernetesCluster.getKubeconfig());
            applicationJson.put("containerRegistryType", containerRegistry.getType());
            applicationJson.put("containerRegistryUrl", containerRegistry.getRegistryUrl());
            applicationJson.put("containerRegistryUsername", containerRegistry.getRegistryUsername());
            applicationJson.put("containerRegistryPassword", containerRegistry.getRegistryPassword());
            applicationJson.put("containerRepositoryName", containerRegistry.getRepository());

            String buildToolSettingId = applicationRequestDto.getBuildToolSettingId();
            if (buildToolSettingId != null && buildToolSettingId != "") {
                final BuildToolSettingsResponseDto buildTool = settingService.getBuildTool(projectResourceId, applicationRequestDto.getBuildToolSettingId());
                applicationJson.put("buildToolType", buildTool.getType());
                applicationJson.put("buildToolSettingsData", buildTool.getFileData());
            }

            application.setData(applicationJson.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        applicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissionUtilsService.canPrincipalReadProjectDetails(#projectResourceId)")
    public Map<String, Long> getDashboardDataForProject(String projectResourceId) {
        long totalApplicationsInProject = applicationRepository.getAllApplicationsByProjectResourceId(projectResourceId);
        long totalDeploymentsInProject = revisionRepository.countAllRevisionsInProject(projectResourceId);
        long totalKubernetesClustersInProject = settingService.countAllKubernetesClustersInProject(projectResourceId);
        long totalContainerRegistriesInProject = settingService.countAllContainerRegistryInProject(projectResourceId);

        Map<String, Long> result = new HashMap<>();
        result.put("totalApplicationsCount", totalApplicationsInProject);
        result.put("totalDeploymentsCount", totalDeploymentsInProject);
        result.put("totalKubernetesClusterCount", totalKubernetesClustersInProject);
        result.put("totalContainerRegistriesCount", totalContainerRegistriesInProject);
        return result;
    }
}
