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
import org.zigmoi.ketchup.application.dtos.ApplicationRequestDto;
import org.zigmoi.ketchup.application.dtos.GitRepoConnectionTestRequestDto;
import org.zigmoi.ketchup.application.entities.Revision;
import org.zigmoi.ketchup.application.services.ApplicationService;
import org.zigmoi.ketchup.common.GitUtility;
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
        return applicationService.listAllRevisionsInProjectWithStatus(projectResourceId, status);
    }

    @GetMapping("/v1-alpha/projects/{project-resource-id}/pipelines/recent")
    @PreAuthorize("@permissionUtilsService.canPrincipalReadProjectDetails(#projectResourceId)")
    public List<Revision> listRecentRevisionPipelinesInProject(
            @PathVariable("project-resource-id") @ValidProjectId String projectResourceId) {
        return applicationService.listRecentRevisionsInProject(projectResourceId);
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
                    //TODO how to make this work with RevisionId, pipeline has only revisionResourceId
                    revision = applicationService.findRevisionByResourceId(revisionId);
                    permissionUtilsService.validatePrincipalCanUpdateApplication(revision.getId().getProjectResourceId());
                    if (revision == null) {
                        log.debug("Revision not found : " + revisionId);
                        return;
                    }
                } catch (Exception e) {
                    return;
                }
                JSONObject parsedStatus = parsePipelineRunResponse(pipelineRunJo.toString());
                if (StringUtility.isNullOrEmpty(parsedStatus.getString("status"))) {
                    return;
                }
                log.info("Tekton event received, event -> \n{}", parsedStatus);
                // success, failed, unknown, running
                if ("success".equalsIgnoreCase(revision.getStatus()) || "failed".equalsIgnoreCase(revision.getStatus())) {
                    return;
                } else if (getDBStatusFromJSON(parsedStatus).equalsIgnoreCase(revision.getStatus())) {
                    return;
                } else {
                    try {
                        String commitId = getCommitIdFromJSON(parsedStatus);
                        if (commitId != null) {
                            revision.setCommitId(commitId);
                        }
                    } catch (Exception e) {
                        log.error("Error in getting commitId, ", e);
                    }
                    try {
                        String helmReleaseVersion = getHelmReleaseVersionFromJSON(parsedStatus);
                        if (helmReleaseVersion != null) {
                            revision.setHelmReleaseVersion(helmReleaseVersion);
                        }
                    } catch (Exception e) {
                        log.error("Error in getting helm release version, ", e);
                    }
                    revision.setStatus(getDBStatusFromJSON(parsedStatus));
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

    private static String getData(String inputJson, String jsonPath) {
        String response = "";
        try {
            response = JsonPath.read(inputJson, jsonPath);
        } catch (Exception e) {
            log.debug("Exception in reading value at specified json path not found. ", e);
        }
        return response;
    }

    public static JSONObject parsePipelineRunResponse(String responseJson) {
        System.out.println("Raw Status Details: " + responseJson);
        JSONObject details = new JSONObject();
        String startTime = getData(responseJson, "$.status.startTime");
        details.put("startTime", startTime);
        String status = getData(responseJson, "$.status.conditions[0].status");
        details.put("status", status);
        String reason = getData(responseJson, "$.status.conditions[0].reason");
        details.put("reason", reason);
        String message = getData(responseJson, "$.status.conditions[0].message");
        details.put("message", message);
        String completionTime = getData(responseJson, "$.status.completionTime");
        details.put("completionTime", completionTime);

        //jsonpath getting parent field using conditions on child.
//        List<Object> test = JsonPath.read(responseJson, "$.status[?(@.taskRuns[?(@.pipelineTaskName == 'build-image')])].taskRuns");
//        System.out.println("test: " + test.get(0).toString());

        JSONArray taskDetails = new JSONArray();
        LinkedHashMap<String, Object> taskRuns = new LinkedHashMap<>();
        try {
            taskRuns = JsonPath.read(responseJson, "$.status.taskRuns");
        } catch (Exception e) {
            log.debug("exception in getting taskruns, ", e);
            details.put("tasks", taskDetails);
            System.out.println("Details: " + details);
            return details;
        }

        for (Map.Entry<String, Object> tr : taskRuns.entrySet()) {
            String taskName = tr.getKey();
            System.out.println("Setting values for task: " + taskName);
            System.out.println("Raw Task Details: " + tr.toString());
            String taskRunJson = new Gson().toJson(tr.getValue());
            System.out.println("Raw Task Details JSON: " + taskRunJson);
            String taskBaseName = getData(taskRunJson, "$.pipelineTaskName");
            String podName = getData(taskRunJson, "$.status.podName");
            String taskStartTime = getData(taskRunJson, "$.status.startTime");
            String taskCompletionTime = getData(taskRunJson, "$.status.completionTime");
            System.out.println("Setting completionTime: " + taskCompletionTime);
            String taskStatus = getData(taskRunJson, "$.status.conditions[0].status");
            System.out.println("Setting status: " + taskStatus);
            String taskReason = getData(taskRunJson, "$.status.conditions[0].reason");
            System.out.println("Setting reason: " + taskReason);
            String taskMessage = getData(taskRunJson, "$.status.conditions[0].message");
            System.out.println("Setting message: " + taskMessage);

            JSONObject taskJson = new JSONObject();
            taskJson.put("name", taskName);
            taskJson.put("baseName", taskBaseName);
            taskJson.put("podName", podName);
            taskJson.put("startTime", taskStartTime);
            taskJson.put("completionTime", taskCompletionTime);
            taskJson.put("status", taskStatus);
            taskJson.put("reason", taskReason);
            taskJson.put("message", taskMessage);

            JSONArray steps;
            try {
                steps = new JSONArray(JsonPath.read(taskRunJson, "$.status.steps").toString());
            } catch (Exception e) {
                log.error("Error in getting steps. ", e);
                taskJson.put("steps", new JSONArray());
                continue;
            }

            System.out.println(steps.length());
            if ("fetch-source-code".equalsIgnoreCase(taskBaseName)) {
                taskJson.put("order", 1);
                String commitId = getData(taskRunJson, "$.status.taskResults[0].value");
                taskJson.put("commitId", commitId);
                JSONArray stepDetails = new JSONArray();
                for (Object stepEntry : steps) {
                    JSONObject step = (JSONObject) stepEntry;
                    String stepName = step.getString("name");
                    if ("clone".equalsIgnoreCase(stepName)) {
                        int order = 1;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    }
                }
                taskJson.put("steps", stepDetails);
            } else if ("build-image".equalsIgnoreCase(taskBaseName)) {
                taskJson.put("order", 2);
                JSONArray stepDetails = new JSONArray();
                for (Object stepEntry : steps) {
                    JSONObject step = (JSONObject) stepEntry;
                    String stepName = step.getString("name");
                    if ("build-and-push".equalsIgnoreCase(stepName)) {
                        int order = 1;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    }
                }
                taskJson.put("steps", stepDetails);
            } else if ("deploy-chart-in-cluster".equalsIgnoreCase(taskBaseName)) {
                taskJson.put("order", 3);
                String taskResult = getData(taskRunJson, "$.status.taskResults[0].value");
                taskJson.put("taskResult", taskResult);
                JSONArray stepDetails = new JSONArray();
                for (Object stepEntry : steps) {
                    JSONObject step = (JSONObject) stepEntry;
                    String stepName = step.getString("name");
                    if ("install-app-in-cluster".equalsIgnoreCase(stepName)) {
                        int order = 1;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    }
                }
                taskJson.put("steps", stepDetails);
            }
            taskDetails.put(taskJson);
            System.out.println("Parsed Task Details: " + taskDetails.toString());
        }
        //details.put("steps", stepDetails);
        details.put("tasks", taskDetails);
        System.out.println("Details: " + details);
        return details;

    }

    private static JSONObject parseStepDetails(Map.Entry<String, Object> tr, String taskBaseName, String podName, JSONObject step, String stepName, int order) {
        JSONObject stepJson = new JSONObject();
        System.out.println(stepJson);
        stepJson.put("order", order);
        stepJson.put("podName", podName);
        stepJson.put("taskName", tr.getKey());
        stepJson.put("taskBaseName", taskBaseName);
        stepJson.put("stepName", stepName);
        stepJson.put("containerName", step.getString("container"));
        if (step.has("waiting")) {
            stepJson.put("state", "Waiting");
            String stepWaitingReason = step.getJSONObject("waiting").getString("reason");
            stepJson.put("reason", stepWaitingReason);
        } else if (step.has("running")) {
            stepJson.put("state", "Running");
            stepJson.put("reason", "");
            String stepStartTime = step.getJSONObject("running").getString("startedAt");
            stepJson.put("startTime", stepStartTime);
        } else if (step.has("terminated")) {
            stepJson.put("state", "Terminated");
            String stepTerminationReason = step.getJSONObject("terminated").getString("reason");
            stepJson.put("reason", stepTerminationReason);
            try {
                String stepTerminationMessage = step.getJSONObject("terminated").getString("message");
                stepJson.put("message", stepTerminationMessage);
            } catch (Exception e) {
                stepJson.put("message", "");
            }
            String stepStartTime = step.getJSONObject("terminated").getString("startedAt");
            stepJson.put("startTime", stepStartTime);
            String stepCompletionTime = step.getJSONObject("terminated").getString("finishedAt");
            stepJson.put("completionTime", stepCompletionTime);
            int stepExitCode = step.getJSONObject("terminated").getInt("exitCode");
            stepJson.put("exitCode", stepExitCode);
            if (stepExitCode == 0) {
                stepJson.put("status", "True");
            } else {
                stepJson.put("status", "False");
            }
        }
        return stepJson;
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

    private String getHelmReleaseVersionFromJSON(JSONObject parsedStatus) {
        JSONArray tasks = parsedStatus.getJSONArray("tasks");
        for (Object oTask : tasks) {
            JSONObject task = (JSONObject) oTask;
            if ("deploy-chart-in-cluster".equalsIgnoreCase(task.getString("baseName"))) {
                String taskResult = task.getString("taskResult");
                System.out.println("taskResult: " + taskResult);
                String helmReleaseVersion = StringUtils.substringBetween(taskResult, "REVISION: ", "NOTES:").trim();
                System.out.println("helmReleaseVersion: " + helmReleaseVersion);
                return helmReleaseVersion;
            }
        }
        return null;
    }

    private String getDBStatusFromJSON(JSONObject parsedStatus) {
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
