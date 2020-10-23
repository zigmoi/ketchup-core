package org.zigmoi.ketchup.application.controllers;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.application.dtos.ApplicationDetailsDto;
import org.zigmoi.ketchup.application.dtos.ApplicationRequestDto;
import org.zigmoi.ketchup.application.dtos.ApplicationResponseDto;
import org.zigmoi.ketchup.application.dtos.GitRepoConnectionTestRequestDto;
import org.zigmoi.ketchup.application.entities.Application;
import org.zigmoi.ketchup.application.entities.ApplicationId;
import org.zigmoi.ketchup.application.entities.Revision;
import org.zigmoi.ketchup.application.entities.RevisionId;
import org.zigmoi.ketchup.application.services.ApplicationService;
import org.zigmoi.ketchup.common.GitUtility;
import org.zigmoi.ketchup.common.GitWebHookParserUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.project.dtos.settings.KubernetesClusterSettingsRequestDto;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class CommonController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/v1-alpha/projects/{project-resource-id}/pipelines")
    public Set<Revision> listAllRevisionPipelinesByStatus(@PathVariable("project-resource-id") String projectResourceId,
                                                          @RequestParam("status") String status) {
        return applicationService.listAllInProjectWithStatus(projectResourceId, status);
    }

    @GetMapping("/v1-alpha/projects/{project-resource-id}/pipelines/recent")
    public List<Revision> listRecentRevisionPipelinesInProject(@PathVariable("project-resource-id") String projectResourceId) {
        return applicationService.listRecentInProject(projectResourceId);
    }

    @PostMapping("/v1-alpha/project/{project-resource-id}/test-connection/git-remote/basic-auth")
    public Map<String, String> testGitConnectivityAndAuthentication(@PathVariable("project-resource-id") String projectResourceId,
                                                                    @RequestBody GitRepoConnectionTestRequestDto requestDto) {
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
                String releaseId = pipelineRunName.substring(("pipeline-run-").length());
                Revision revision = null;
                try {
                    //TODO how to make this work with ReleaseId, pipeline has only releaseResourceId
                    revision = applicationService.findByReleaseResourceId(releaseId);
                    if (revision == null) {
                        log.debug("Revision not found : " + releaseId);
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
                    String commitId = getCommitIdFromJSON(parsedStatus);
                    if (commitId != null) {
                        revision.setCommitId(commitId);
                    }
                    revision.setStatus(getDBStatusFromJSON(parsedStatus));
                    revision.setPipelineStatusJson(parsedStatus.toString());
                    applicationService.update(revision);
                    log.debug("Revision status updated, release -> \n{}", revision.toString());
                    if ("success".equalsIgnoreCase(revision.getStatus()) || "failed".equalsIgnoreCase(revision.getStatus())) {
                        try {
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        applicationService.cleanPipelineResources(revision.getId());
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
