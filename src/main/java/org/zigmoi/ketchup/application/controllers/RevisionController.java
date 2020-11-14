package org.zigmoi.ketchup.application.controllers;

import com.google.common.io.ByteStreams;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
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
import org.zigmoi.ketchup.application.entities.ApplicationId;
import org.zigmoi.ketchup.application.entities.Revision;
import org.zigmoi.ketchup.application.entities.RevisionId;
import org.zigmoi.ketchup.application.services.ApplicationService;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.common.validations.ValidResourceId;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
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
                                              @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId) {
        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        return Collections.singletonMap("revisionResourceId", applicationService.createRevision(applicationId));
    }

    @GetMapping("/{revision-resource-id}")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public Revision getRevision(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId) {
        RevisionId revisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        return applicationService.findRevisionById(revisionId);
    }

    @GetMapping
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public Set<Revision> listAllRevisions(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                          @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId) {
        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        return applicationService.listAllRevisionsInApplication(applicationId);
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
    public Revision refreshRevisionPipelineStatus(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                  @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                  @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId) throws IOException, ApiException {
        RevisionId id = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        Revision revision = applicationService.findRevisionById(id);
        String pipelineRunName = "pipeline-run-".concat(revisionResourceId);
        String kubeConfig = getKubeConfig(revision.getApplicationDataJson());
        String namespace = getKubernetesNamespace(revision.getApplicationDataJson());
        JSONObject parsedStatus = KubernetesUtility.getPipelineRunStatus(kubeConfig, namespace, pipelineRunName);
        if (parsedStatus == null) {
            return revision;
        }
        String commitId = getCommitIdFromJSON(parsedStatus);
        if (commitId != null) {
            revision.setCommitId(commitId);
        }
        revision.setStatus(parsePipelineStatusFromJSON(parsedStatus));
        revision.setPipelineStatusJson(parsedStatus.toString());
        applicationService.updateRevision(revision);
        return revision;
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
        RevisionId revisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        Revision revision = applicationService.findRevisionById(revisionId);
        String kubeConfig = getKubeConfig(revision.getApplicationDataJson());
        String namespace = getKubernetesNamespace(revision.getApplicationDataJson());
        SseEmitter emitter = new SseEmitter();
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
    public void streamRevisionPipelineLogsDirect(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                 @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                 @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId,
                                                 @RequestParam("podName") @NotBlank @Size(max = 250) String podName,
                                                 @RequestParam("containerName") @NotBlank @Size(max = 250) String containerName,
                                                 @RequestParam(value = "tailLines", required = false) Integer tailLines,
                                                 HttpServletResponse response) throws IOException, ApiException {
        RevisionId revisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        if ("1".equalsIgnoreCase(containerName)) {
            containerName = null;
        }
        try (InputStream logStream = getLogsInputStream(revisionId, podName, containerName, tailLines)) {
            //noinspection UnstableApiUsage
            ByteStreams.copy(logStream, response.getOutputStream());
        }
    }

    @GetMapping(value = "/{revision-resource-id}/pipeline/logs/stream/sse")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public SseEmitter streamRevisionPipelineLogsSSE(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                                    @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                                    @PathVariable("revision-resource-id") @ValidResourceId String revisionResourceId,
                                                    @RequestParam("podName") @NotBlank @Size(max = 250) String podName,
                                                    @RequestParam("containerName") @NotBlank @Size(max = 250) String containerName,
                                                    @RequestParam(value = "tailLines", required = false) Integer tailLines) {
        RevisionId revisionId = new RevisionId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId, revisionResourceId);
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getLogsInputStream(revisionId, podName, containerName, tailLines)))) {
                String response;
                while ((response = reader.readLine()) != null) {
                    SseEmitter.SseEventBuilder eventBuilderDataStream = SseEmitter.event().data(response, MediaType.TEXT_PLAIN);
                    emitter.send(eventBuilderDataStream);
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                emitter.complete();
            }
        });
        return emitter;
    }

    @GetMapping(value = "/active/application-logs/stream")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public void streamAppLogsForActiveRevision(@PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
                                               @PathVariable("application-resource-id") @ValidResourceId String applicationResourceId,
                                               @RequestParam("podName") @NotBlank @Size(max = 250) String podName,
                                               @RequestParam("containerName") @NotBlank @Size(max = 250) String containerName,
                                               @RequestParam(value = "tailLines", required = false) Integer tailLines,
                                               HttpServletResponse response) throws IOException, ApiException {

        ApplicationId applicationId = new ApplicationId(AuthUtils.getCurrentTenantId(), projectResourceId, applicationResourceId);
        Revision revision = applicationService.getActiveRevision(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Active revision found for application with ID: " + applicationResourceId));

        if (!podName.startsWith("app-" + applicationResourceId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You dont have sufficient access to fetch logs for this revision.");
        }
        if ("1".equalsIgnoreCase(containerName)) {
            containerName = null;
        }
        try (InputStream logStream = getLogsInputStream(revision.getId(), podName, containerName, tailLines)) {
            //noinspection UnstableApiUsage
            ByteStreams.copy(logStream, response.getOutputStream());
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

    private String getCommitIdFromJSON(JSONObject parsedStatus) {
        JSONArray tasks = parsedStatus.getJSONArray("tasks");
        for (Object oTask : tasks) {
            JSONObject task = (JSONObject) oTask;
            if ("fetch-source-code".equalsIgnoreCase(task.getString("baseName"))) {
                return task.getString("commitId");
            }
        }
        return null;
    }

    private String parsePipelineStatusFromJSON(JSONObject parsedStatus) {
        String statusPipeline = parsedStatus.getString("status");
        String statusReasonPipeline = parsedStatus.getString("reason");
        if (statusPipeline.equalsIgnoreCase("True")) {
            return "SUCCESS";
        } else if (statusPipeline.equalsIgnoreCase("Unknown")
                || statusReasonPipeline.equalsIgnoreCase("Running")) {
            return "IN PROGRESS";
        } else if (statusPipeline.equalsIgnoreCase("False")) {
            return "FAILED";
        } else {
            return "UNKNOWN";
        }
    }
}
