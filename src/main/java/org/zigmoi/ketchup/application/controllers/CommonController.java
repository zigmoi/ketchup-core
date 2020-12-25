package org.zigmoi.ketchup.application.controllers;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.zigmoi.ketchup.application.dtos.GitRepoConnectionTestRequestDto;
import org.zigmoi.ketchup.application.entities.Revision;
import org.zigmoi.ketchup.application.services.ApplicationService;
import org.zigmoi.ketchup.application.services.PipelineUtils;
import org.zigmoi.ketchup.common.GitUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.project.services.PermissionUtilsService;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Validated
@Slf4j
@RestController
public class CommonController {

    @Autowired
    private Validator validator;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PermissionUtilsService permissionUtilsService;

    @GetMapping("/v1-alpha/projects/{project-resource-id}/dashboard-data")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadProjectDetails(#projectResourceId)")
    public Map<String, Long> getDashboardDataForProject(
            @PathVariable("project-resource-id") @ValidProjectId String projectResourceId) {
        return applicationService.getDashboardDataForProject(projectResourceId);
    }

    @GetMapping("/v1-alpha/projects/{project-resource-id}/pipelines")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadProjectDetails(#projectResourceId)")
    public Set<Revision> listAllRevisionPipelinesByStatusInProject(
            @PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
            @RequestParam("status") @NotBlank @Size(max = 100) String status) {
        return applicationService.listAllRevisionPipelinesInProjectWithStatus(projectResourceId, status);
    }

    @GetMapping("/v1-alpha/projects/{project-resource-id}/pipelines/recent")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadProjectDetails(#projectResourceId)")
    public List<Revision> listRecentRevisionPipelinesInProject(
            @PathVariable("project-resource-id") @ValidProjectId String projectResourceId) {
        return applicationService.listRecentRevisionPipelinesInProject(projectResourceId);
    }

    @PostMapping("/v1-alpha/projects/{project-resource-id}/git-repo/test-connection")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadApplication(#projectResourceId)")
    public Map<String, String> testGitConnectivityAndAuthentication(
            @PathVariable("project-resource-id") @ValidProjectId String projectResourceId,
            @RequestBody GitRepoConnectionTestRequestDto requestDto) {

        Set<ConstraintViolation<GitRepoConnectionTestRequestDto>> violations = validator.validate(requestDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        boolean connectionSuccessful = false;
        try {
            connectionSuccessful = GitUtility.instance().testConnection(requestDto.getUsername(), requestDto.getPassword(), requestDto.getRepoUrl());
        } catch (Exception e) {
            connectionSuccessful = false;
        }
        Map<String, String> status = new HashMap<>();
        status.put("status", connectionSuccessful ? "success" : "failed");
        return status;
    }

    //TODO find better place for this api, its global (independent of project)
    //tekton cluster has only one url for sending all its events.
    @PostMapping("/v1-alpha/applications/revisions/pipeline/tekton-events")
    public void handlePipelineStatusUpdateEvents(HttpEntity<String> request) {
        try {
            JSONObject inJo = new JSONObject(Objects.requireNonNull(request.getBody()));
            if (!inJo.has("pipelineRun")) {
                return;
            }
            JSONObject pipelineRunJo = inJo.getJSONObject("pipelineRun");
            if (pipelineRunJo != null) {
                String pipelineRunName = pipelineRunJo.getJSONObject("metadata").getString("name");
                String revisionId = pipelineRunName.substring(("pipeline-run-").length());
                Revision revision = null;
                try {
                    //TODO how to make this work with
                    // RevisionId(tenantId+projectResourceId+applicationResourceId+RevisionResourceId),
                    // pipeline has only revisionResourceId
                    revision = applicationService.findRevisionByResourceId(revisionId);
                    permissionUtilsService.validatePrincipalCanUpdateApplication(revision.getId().getProjectResourceId());
                    if (revision == null) {
                        log.debug("Revision not found : " + revisionId);
                        return;
                    }
                } catch (Exception e) {
                    return;
                }
                JSONObject parsedStatus = PipelineUtils.parsePipelineRunResponse(pipelineRunJo.toString());
                if (StringUtility.isNullOrEmpty(parsedStatus.getString("status"))) {
                    return;
                }
                log.info("Tekton event received, event -> \n{}", parsedStatus);
                // success, failed, unknown, running
                if ("success".equalsIgnoreCase(revision.getStatus()) || "failed".equalsIgnoreCase(revision.getStatus())) {
                    return;
                } else if (PipelineUtils.parsePipelineStatusFromJSON(parsedStatus).equalsIgnoreCase(revision.getStatus())) {
                    return;
                } else {
                    try {
                        String commitId = PipelineUtils.getCommitIdFromJSON(parsedStatus);
                        if (commitId != null) {
                            revision.setCommitId(commitId);
                        }
                    } catch (Exception e) {
                        log.error("Error in getting commitId, ", e);
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
                    log.debug("Revision status updated, revision -> \n{}", revision.toString());
                    if ("success".equalsIgnoreCase(revision.getStatus()) || "failed".equalsIgnoreCase(revision.getStatus())) {
                        try {
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        applicationService.cleanPipelineResourcesInRevision(revision.getId());
                        log.info("Revision resources cleaned up, revision -> \n{}", revision.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.info("Tekton event received, failed parsing.", e);
        }
    }


}
