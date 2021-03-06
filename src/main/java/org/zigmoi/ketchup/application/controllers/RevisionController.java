package org.zigmoi.ketchup.application.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.zigmoi.ketchup.application.dtos.ApplicationDetailsDto;
import org.zigmoi.ketchup.application.dtos.RevisionBasicResponseDto;
import org.zigmoi.ketchup.application.dtos.RevisionResponseDto;
import org.zigmoi.ketchup.application.entities.*;
import org.zigmoi.ketchup.application.services.ApplicationService;
import org.zigmoi.ketchup.application.services.DeploymentTriggerType;
import org.zigmoi.ketchup.application.services.PipelineUtils;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.common.validations.ValidResourceId;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Validated
@Slf4j
@RestController
@RequestMapping("/v1-alpha/projects/{project-resource-id}/applications/{application-resource-id}/revisions")
public class RevisionController {

    private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PermissionUtilsService permissionUtilsService;

    @PostMapping
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#projectResourceId)")
    public Map<String, String> createRevision(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                              @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                              @RequestParam("commit-id") @NotBlank @Size(max = 100) String commitId) {
        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        return Collections.singletonMap("revisionResourceId",
                applicationService.createRevision(DeploymentTriggerType.MANUAL.toString(), commitId, applicationId));
    }

    @GetMapping("/{revision-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public RevisionResponseDto getRevision(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId) {
        RevisionId revisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        Revision revision = applicationService.findRevisionById(revisionId);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        RevisionResponseDto revisionResponseDto = null;
        try {
            revisionResponseDto = objectMapper.readValue(revision.getApplicationDataJson(), RevisionResponseDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        revisionResponseDto.setRevisionId(revision.getId());
        revisionResponseDto.setVersion(revision.getVersion());
        revisionResponseDto.setStatus(revision.getStatus());
        revisionResponseDto.setDeploymentTriggerType(revision.getDeploymentTriggerType());
        revisionResponseDto.setErrorMessage(revision.getErrorMessage());
        revisionResponseDto.setCommitId(revision.getCommitId());
        revisionResponseDto.setHelmChartId(revision.getHelmChartId());
        revisionResponseDto.setHelmReleaseId(revision.getHelmReleaseId());
        revisionResponseDto.setHelmReleaseVersion(revision.getHelmReleaseVersion());
        revisionResponseDto.setRollback(revision.isRollback());
        revisionResponseDto.setOriginalRevisionVersionId(revision.getOriginalRevisionVersionId());
        revisionResponseDto.setCreatedBy(revision.getCreatedBy());
        revisionResponseDto.setCreatedOn(revision.getCreatedOn());
        revisionResponseDto.setLastUpdatedBy(revision.getLastUpdatedBy());
        revisionResponseDto.setLastUpdatedOn(revision.getLastUpdatedOn());
        return revisionResponseDto;
    }

    @GetMapping
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public List<RevisionBasicResponseDto> listAllRevisions(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                           @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                           @RequestParam (required = false) Boolean full) {
        if (full == null) full = Boolean.TRUE;
        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        return applicationService.listAllRevisionsInApplication(applicationId, full);
    }

    @GetMapping("/{revision-resource-id}/rollback")
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#projectResourceId)")
    public void rollbackRevision(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                 @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                 @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId) {
        //rollback current application to revision version as in revisionResourceId.
        RevisionId targetRevisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        applicationService.rollbackToRevision(targetRevisionId);
    }

    @GetMapping("/{revision-resource-id}/pipeline/status/refresh")
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#projectResourceId)")
    public void refreshRevisionPipelineStatus(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                  @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                  @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId) throws IOException, ApiException {
        RevisionId id = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        Revision revision = applicationService.findRevisionById(id);
        if ("success".equalsIgnoreCase(revision.getStatus()) || "failed".equalsIgnoreCase(revision.getStatus())) {
            //ignore as db already has updated status.
            return;
        }
        if (revision.isRollback()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operation not supported, cannot refresh status of rollback revision.");
        }

        String pipelineRunName = "pipeline-run-".concat(revisionResourceId);
        String kubeConfig = getKubeConfig(revision.getApplicationDataJson());
        String namespace = getKubernetesNamespace(revision.getApplicationDataJson());
        JSONObject parsedStatus = KubernetesUtility.getPipelineRunStatus(kubeConfig, namespace, pipelineRunName);
        if (parsedStatus == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot refresh status, no status found, please try again later.");
        }
        String commitId = PipelineUtils.getCommitIdFromJSON(parsedStatus);
        if (commitId != null) {
            revision.setCommitId(commitId);
        }
        try {
            String helmReleaseVersion = PipelineUtils.getHelmReleaseVersionFromJSON(parsedStatus);
            if (helmReleaseVersion != null) {
                revision.setHelmReleaseVersion(helmReleaseVersion);
            }
        } catch (Exception e) {
            log.error("Error in getting helm release version, ", e);
        }
        revision.setStatus(PipelineUtils.parsePipelineStatusFromJSON(parsedStatus));
        revision.setPipelineStatusJson(parsedStatus.toString());
        applicationService.updateRevision(revision);
    }

    @GetMapping("/{revision-resource-id}/pipeline/stop")
    @PreAuthorize("@permissionUtilsService.canPrincipalUpdateApplication(#projectResourceId)")
    public void stopRevisionPipeline(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                     @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                     @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId) {
        RevisionId id = new RevisionId(AuthUtils.getCurrentTenantId(),
                projectResourceId, applicationResourceId, revisionResourceId);
        applicationService.stopRevisionPipeline(id);
    }

    @DeleteMapping("/{revision-resource-id}/pipeline/cleanup-cluster-resources")
    @PreAuthorize("@permissionUtilsService.canPrincipalDeleteApplication(#projectResourceId)")
    public void cleanUpRevisionPipelineResourcesInCluster(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                          @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                          @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId) {
        RevisionId revisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        applicationService.cleanPipelineResourcesInRevision(revisionId);
    }

    @GetMapping("/{revision-resource-id}/pipeline/status/stream")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public SseEmitter streamRevisionPipelineStatus(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                   @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                   @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId) {
        SseEmitter emitter = new SseEmitter(300_000L); //server will break connection after 300 sec.
        RevisionId revisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        Revision revision = applicationService.findRevisionById(revisionId);
        if (revision.isRollback()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operation not supported, no pipeline is associated with rollback revision.");
        }
        String kubeConfig = getKubeConfig(revision.getApplicationDataJson());
        String namespace = getKubernetesNamespace(revision.getApplicationDataJson());
        if ("SUCCESS".equalsIgnoreCase(revision.getStatus()) || "FAILED".equalsIgnoreCase(revision.getStatus())) {
            nonBlockingService.execute(() -> {
                try {
                    SseEmitter.SseEventBuilder eventBuilderDataStream =
                            SseEmitter.event()
                                    .name("data")
                                    .reconnectTime(10_000)
                                    .data(revision.getPipelineStatusJson());
                    emitter.send(eventBuilderDataStream);
                    SseEmitter.SseEventBuilder eventBuilderCloseStream =
                            SseEmitter.event()
                                    .name("close")
                                    .data("");
                    emitter.send(eventBuilderCloseStream);
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            });
        } else {
            String pipelineRunName = "pipeline-run-".concat(revisionResourceId);
            nonBlockingService.execute(() -> {
                try {
                    KubernetesUtility.watchAndStreamPipelineRunStatus(kubeConfig, namespace, pipelineRunName, emitter);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                    log.error(ex.getLocalizedMessage(), ex);
                }
            });
        }
        return emitter;
    }

    @GetMapping(value = "/{revision-resource-id}/pipeline/logs/stream/direct")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public void streamRevisionPipelineLogs(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                           @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                           @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId,
                                           @RequestParam("podName") @NotBlank @Size(max = 250) String podName,
                                           @RequestParam("containerName") @NotBlank @Size(max = 250) String containerName,
                                           @RequestParam(value = "tailLines", required = false) Integer tailLines,
                                           HttpServletResponse response) throws IOException, ApiException {
        validatePipelinePodLogAccess(revisionResourceId, podName);
        RevisionId revisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        if ("1".equalsIgnoreCase(containerName)) {
            containerName = null;
        }

        ServletOutputStream os = response.getOutputStream();
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        try (InputStream logStream = getLogsInputStream(revisionId, podName, containerName, tailLines)) {
            byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            while ((bytesRead = logStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                os.flush();
                response.flushBuffer();
                // System.out.write(buffer);
            }
            os.close();
        }
    }

    private void validateApplicationPodLogAccess(String applicationResourceId, String podName) {
        String releaseName = "app-" + applicationResourceId;
        if (podName.startsWith(releaseName) == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied, pod should belong to requested application.");
        }
    }

    private void validatePipelinePodLogAccess(String revisionResourceId, String podName) {
        String releaseName = "pipeline-run-" + revisionResourceId;
        if (podName.startsWith(releaseName) == false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied, pod should belong to this requested pipeline.");
        }
    }

    @GetMapping(value = "/current/application-logs/stream")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public void streamAppLogsForCurrentRevision(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                                                @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                                                @RequestParam("podName") @NotBlank @Size(max = 250) String podName,
                                                                                @RequestParam("containerName") @NotBlank @Size(max = 250) String containerName,
                                                                                @RequestParam(value = "tailLines", required = false) Integer tailLines,
                                                                                HttpServletResponse response) throws ApiException, IOException {

        validateApplicationPodLogAccess(applicationResourceId, podName);
        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        Revision revision = applicationService.getCurrentRevision(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current revision not found for application with ID: " + applicationResourceId));

        if (!podName.startsWith("app-" + applicationResourceId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You dont have sufficient access to fetch logs for this revision.");
        }
        if ("1".equalsIgnoreCase(containerName)) {
            containerName = null;
        }

        ServletOutputStream os = response.getOutputStream();
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        try (InputStream logStream = getLogsInputStream(revision.getId(), podName, containerName, tailLines)) {
            byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            while ((bytesRead = logStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                os.flush();
                response.flushBuffer();
               // System.out.write(buffer);
            }
            os.close();
        }
    }

    private String getKubeConfig(String applicationDataJson) {
        JSONObject jsonObject = new JSONObject(applicationDataJson);
        return StringUtility.decodeBase64((jsonObject.getString("devKubeconfig")));
    }

    private String getKubernetesNamespace(String applicationDataJson) {
        JSONObject jsonObject = new JSONObject(applicationDataJson);
        return jsonObject.getString("devKubernetesNamespace");
    }

    private InputStream getLogsInputStream(RevisionId revisionId, String podName, String containerName, Integer tailLines) throws IOException, ApiException {
        Revision revision = applicationService.findRevisionById(revisionId);
        ApplicationDetailsDto applicationDetailsDto = applicationService.extractApplicationByRevisionId(revision);
        return KubernetesUtility.getPodLogs(StringUtility.decodeBase64(applicationDetailsDto.getDevKubeconfig()),
                applicationDetailsDto.getDevKubernetesNamespace(), podName, containerName, tailLines);
    }

    @GetMapping(value = "/{revision-resource-id}/artifacts/get")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public Set<PipelineArtifact> getRevisionArtifacts(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                      @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                      @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId) {
        RevisionId revisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        Set<PipelineArtifact> artifacts = applicationService.listAllPipelineArtifactsInRevision(revisionId);
        artifacts.forEach(a -> a.setResourceContent(""));
        return artifacts;
    }

    @GetMapping(value = "/{revision-resource-id}/artifacts/get/{artifact-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public PipelineArtifact getRevisionArtifacts(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                 @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                 @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId,
                                                 @PathVariable("artifact-resource-id") @ValidResourceId String pipelineArtifactResourceId) {
        PipelineArtifactId artifactId = new PipelineArtifactId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId, pipelineArtifactResourceId);
        return applicationService.getPipelineArtifactsById(artifactId);
    }
}
