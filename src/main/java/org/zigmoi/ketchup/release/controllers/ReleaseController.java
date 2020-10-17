package org.zigmoi.ketchup.release.controllers;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.zigmoi.ketchup.common.GitWebHookParserUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.deployment.dtos.DeploymentDetailsDto;
import org.zigmoi.ketchup.deployment.services.DeploymentService;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;
import org.zigmoi.ketchup.release.services.ReleaseService;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class ReleaseController {

    private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    @Autowired
    private ReleaseService releaseService;
    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ResourceLoader resourceLoader;

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

    @PostMapping("/v1/release")
    public Map<String, String> createRelease(@RequestParam("deploymentId") String deploymentResourceId) {
        return Collections.singletonMap("releaseResourceId", releaseService.create(deploymentResourceId));
    }

    @GetMapping("/v1/release")
    public Release getRelease(@RequestParam("releaseResourceId") String releaseResourceId) {
        return releaseService.findById(releaseResourceId);
    }

    @GetMapping("/v1/release/rollback")
    public void rollbackRelease(@RequestParam("releaseResourceId") String releaseResourceId) {
        //rollback current application to release version as in releaseResourceId.
        releaseService.rollback(releaseResourceId);
    }

    @GetMapping("/v1/release/active")
    public Release getActiveRelease(@RequestParam("deploymentResourceId") String deploymentResourceId) {
        return releaseService.getActiveRelease(deploymentResourceId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active release not found."));
    }

    @GetMapping("/v1/release/refresh")
    public Release refreshReleaseStatus(@RequestParam("releaseResourceId") String releaseResourceId) throws IOException, ApiException {
        Release release = releaseService.findById(releaseResourceId);
        String pipelineRunName = "pipeline-run-".concat(releaseResourceId);
        String kubeConfig = getKubeConfig(release.getDeploymentDataJson());
        String namespace = getKubernetesNamespace(release.getDeploymentDataJson());
        JSONObject parsedStatus = KubernetesUtility.getPipelineRunStatus(kubeConfig, namespace, pipelineRunName);
        if (parsedStatus == null) {
            return release;
        }
        String commitId = getCommitIdFromJSON(parsedStatus);
        if (commitId != null) {
            release.setCommitId(commitId);
        }
        release.setStatus(getDBStatusFromJSON(parsedStatus));
        release.setPipelineStatusJson(parsedStatus.toString());
        releaseService.update(release);
        return release;
    }

    @GetMapping("/v1/release/stop")
    public void stopRelease(@RequestParam("releaseResourceId") String releaseResourceId) {
        releaseService.stop(releaseResourceId);
    }

    @GetMapping("/v1/releases")
    public Set<Release> listAllReleasesInDeployment(@RequestParam("deploymentId") String deploymentResourceId) {
        return releaseService.listAllInDeployment(deploymentResourceId);
    }

    @DeleteMapping("/v1/release/pipeline/cleanup")
    public void deletePipelineResources(@RequestParam("releaseId") String releaseResourceId) {
        releaseService.cleanPipelineResources(new ReleaseId(AuthUtils.getCurrentTenantId(), releaseResourceId));
    }

    @GetMapping("/v1/pipelines")
    public Set<Release> listAllReleasesInDeployment(@RequestParam("projectResourceId") String projectResourceId, @RequestParam("status") String status) {
        return releaseService.listAllInProjectWithStatus(projectResourceId, status);
    }

    @GetMapping("/v1/pipelines/recent")
    public Set<Release> listRecentReleases(@RequestParam("projectResourceId") String projectResourceId) {
        return releaseService.listRecentInProject(projectResourceId);
    }

    @GetMapping("/v1/release/pipeline/tekton-events")
    public void pipelineTektonEventsGet() {
        log.info("Tekton event received");
    }

    @PostMapping("/v1/release/pipeline/tekton-events")
    public void pipelineTektonEventsPost(HttpEntity<String> request) {
        try {
            JSONObject inJo = new JSONObject(Objects.requireNonNull(request.getBody()));
            if (!inJo.has("pipelineRun")) {
                return;
            }
            JSONObject pipelineRunJo = inJo.getJSONObject("pipelineRun");
            if (pipelineRunJo != null) {
                String pipelineRunName = pipelineRunJo.getJSONObject("metadata").getString("name");
                String releaseId = pipelineRunName.substring(("pipeline-run-").length());
                Release release = null;
                try {
                    release = releaseService.findById(releaseId);
                    if (release == null) {
                        log.debug("Release info not found : " + releaseId);
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
                if ("success".equalsIgnoreCase(release.getStatus()) || "failed".equalsIgnoreCase(release.getStatus())) {
                    return;
                } else if (getDBStatusFromJSON(parsedStatus).equalsIgnoreCase(release.getStatus())) {
                    return;
                } else {
                    String commitId = getCommitIdFromJSON(parsedStatus);
                    if (commitId != null) {
                        release.setCommitId(commitId);
                    }
                    release.setStatus(getDBStatusFromJSON(parsedStatus));
                    release.setPipelineStatusJson(parsedStatus.toString());
                    releaseService.update(release);
                    log.debug("Release status updated, release -> \n{}", release.toString());
                    if ("success".equalsIgnoreCase(release.getStatus()) || "failed".equalsIgnoreCase(release.getStatus())) {
                        try {
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        releaseService.cleanPipelineResources(release.getId());
                        log.info("Release resources cleaned up, release -> \n{}", release.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.info("Tekton event received, failed parsing.", e);
        }
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

    @GetMapping("/v1/release/pipeline/status/stream/sse")
    public SseEmitter streamPipelineStatus(@RequestParam("releaseId") String releaseResourceId) {
        Release release = releaseService.findById(releaseResourceId);
        String kubeConfig = getKubeConfig(release.getDeploymentDataJson());
        String namespace = getKubernetesNamespace(release.getDeploymentDataJson());
        SseEmitter emitter = new SseEmitter();
        if ("SUCCESS".equalsIgnoreCase(release.getStatus()) || "FAILED".equalsIgnoreCase(release.getStatus())) {
            nonBlockingService.execute(() -> {
                try {
                    SseEmitter.SseEventBuilder eventBuilderDataStream =
                            SseEmitter.event()
                                    .name("data")
                                    .reconnectTime(10_000)
                                    .data(release.getPipelineStatusJson());
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
            String pipelineRunName = "pipeline-run-".concat(releaseResourceId);
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

    private String getKubeConfig(String deploymentDataJson) {
        JSONObject jo = new JSONObject(deploymentDataJson);
        return StringUtility.decodeBase64((jo.getString("devKubeconfig")));
    }

    private String getKubernetesNamespace(String deploymentDataJson) {
        JSONObject jo = new JSONObject(deploymentDataJson);
        return jo.getString("devKubernetesNamespace");
    }

    private InputStream getLogsInputStream(String releaseResourceId, String podName, String containerName, Integer tailLines) throws IOException, ApiException {
        Release release = releaseService.findById(releaseResourceId);
        DeploymentDetailsDto deploymentDetailsDto = releaseService.extractDeployment(release);
        return KubernetesUtility.getPodLogs(StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig()),
                deploymentDetailsDto.getDevKubernetesNamespace(), podName, containerName, tailLines);
    }

    //    @GetMapping(value = "/v1/release/pipeline/pod-container/logs/stream/direct")
    @GetMapping(value = "/v1/release/pipeline/logs/stream/direct")
    public void streamPipelineLogsDirect(HttpServletResponse response,
                                         @RequestParam("releaseId") String releaseResourceId,
                                         @RequestParam("podName") String podName,
                                         @RequestParam("containerName") String containerName,
                                         @RequestParam(value = "tailLines", required = false) Integer tailLines) throws IOException, ApiException {
        if ("1".equalsIgnoreCase(containerName)) {
            containerName = null;
        }
        try (InputStream logStream = getLogsInputStream(releaseResourceId, podName, containerName, tailLines)) {
            //noinspection UnstableApiUsage
            ByteStreams.copy(logStream, response.getOutputStream());
        }
    }


    //    @GetMapping(value = "/v1/release/pipeline/pod-container/logs/stream/sse")
    @GetMapping(value = "/v1/release/pipeline/logs/stream/sse")
    public SseEmitter streamPipelineLogsSSE(@RequestParam("releaseId") String releaseResourceId,
                                            @RequestParam("podName") String podName,
                                            @RequestParam("containerName") String containerName,
                                            @RequestParam(value = "tailLines", required = false) Integer tailLines) {
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getLogsInputStream(releaseResourceId, podName, containerName, tailLines)))) {
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

    @GetMapping(value = "/v1/release/active/application/logs/stream")
    public void streamActiveReleaseLogs(HttpServletResponse response,
                                        @RequestParam("deploymentResourceId") String deploymentResourceId,
                                        @RequestParam("podName") String podName,
                                        @RequestParam("containerName") String containerName,
                                        @RequestParam(value = "tailLines", required = false) Integer tailLines) throws IOException, ApiException {
        Release release = releaseService.getActiveRelease(deploymentResourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Active release found for deployment with ID: " + deploymentResourceId));
        String releaseResourceId = release.getId().getReleaseResourceId();

        if (!podName.startsWith("release-" + deploymentResourceId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You dont have sufficient access to fetch logs for this release.");
        }
        if ("1".equalsIgnoreCase(containerName)) {
            containerName = null;
        }
        try (InputStream logStream = getLogsInputStream(releaseResourceId, podName, containerName, tailLines)) {
            //noinspection UnstableApiUsage
            ByteStreams.copy(logStream, response.getOutputStream());
        }
    }

    @PostMapping(value = "/v1/release/git-webhook/{vendor}/listen")
    public void gitWebhookListenPost(HttpServletResponse response, @PathVariable String vendor,
                                     @RequestParam("uid") String deploymentResourceId, @RequestBody(required = false) String req) {
        DeploymentDetailsDto deploymentDetailsDto = deploymentService.getDeployment(deploymentResourceId);
        if (deploymentDetailsDto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        GitWebHookParserUtility.WebhookEvent event = GitWebHookParserUtility.parseEvent(vendor, deploymentResourceId, req);
        log.info("Received Webhook Event : " + event.toString());

        nonBlockingService.submit(() -> {
            if (GitWebHookParserUtility.isPushEvent(event)
                    && deploymentDetailsDto.getGitRepoBranchName().equalsIgnoreCase(event.getBranchName())) {
                try {
                    releaseService.create(deploymentResourceId);
                    log.info("Invoked redeploy for Webhook Event : " + event.toString());
                } catch (Exception e) {
                    log.error("Failed redeploy for Webhook Event : " + event.toString(), e);
                }
            } else {
                log.info("Ignored Webhook Event : " + event.toString());
            }
        });

    }

    @GetMapping(value = "/v1/release/git-webhook/{vendor}/listener-url")
    public Map<String, String> gitWebhookGenerateListenerURL(HttpServletResponse response, @PathVariable String vendor,
                                              @RequestParam("deploymentId") String deploymentResourceId) {
        String url = releaseService.generateGitWebhookListenerURL(vendor, deploymentResourceId);
        Map<String, String> map = new HashMap<>();
        map.put("webhookUrl", url);
        return map;
    }
}
