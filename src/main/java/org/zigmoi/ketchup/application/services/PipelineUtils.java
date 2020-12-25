package org.zigmoi.ketchup.application.services;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zigmoi.ketchup.common.KubernetesUtility;

import java.util.LinkedHashMap;
import java.util.Map;

public class PipelineUtils {
    private final static Logger logger = LoggerFactory.getLogger(PipelineUtils.class);

    public static String getCommitIdFromJSON(JSONObject parsedStatus) {
        JSONArray tasks = parsedStatus.getJSONArray("tasks");
        for (Object oTask : tasks) {
            JSONObject task = (JSONObject) oTask;
            if ("fetch-source-code".equalsIgnoreCase(task.getString("baseName"))) {
                return task.getString("commitId");
            }
        }
        return null;
    }

    public static String getHelmReleaseVersionFromJSON(JSONObject parsedStatus) {
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

    public static String parsePipelineStatusFromJSON(JSONObject parsedStatus) {
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

    public static JSONObject parsePipelineRunResponse(String responseJson) {
        System.out.println("Raw Status Details: " + responseJson);
        JSONObject details = new JSONObject();
        String startTime = KubernetesUtility.getData(responseJson, "$.status.startTime");
        details.put("startTime", startTime);
        String status = KubernetesUtility.getData(responseJson, "$.status.conditions[0].status");
        details.put("status", status);
        String reason = KubernetesUtility.getData(responseJson, "$.status.conditions[0].reason");
        details.put("reason", reason);
        String message = KubernetesUtility.getData(responseJson, "$.status.conditions[0].message");
        details.put("message", message);
        String completionTime = KubernetesUtility.getData(responseJson, "$.status.completionTime");
        details.put("completionTime", completionTime);

        //jsonpath getting parent field using conditions on child.
//        List<Object> test = JsonPath.read(responseJson, "$.status[?(@.taskRuns[?(@.pipelineTaskName == 'build-image')])].taskRuns");
//        System.out.println("test: " + test.get(0).toString());

        JSONArray taskDetails = new JSONArray();
        LinkedHashMap<String, Object> taskRuns = new LinkedHashMap<>();
        try {
            taskRuns = JsonPath.read(responseJson, "$.status.taskRuns");
        } catch (Exception e) {
            logger.debug("exception in getting taskruns, ", e);
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
            String taskBaseName = KubernetesUtility.getData(taskRunJson, "$.pipelineTaskName");
            String podName = KubernetesUtility.getData(taskRunJson, "$.status.podName");
            String taskStartTime = KubernetesUtility.getData(taskRunJson, "$.status.startTime");
            String taskCompletionTime = KubernetesUtility.getData(taskRunJson, "$.status.completionTime");
            System.out.println("Setting completionTime: " + taskCompletionTime);
            String taskStatus = KubernetesUtility.getData(taskRunJson, "$.status.conditions[0].status");
            System.out.println("Setting status: " + taskStatus);
            String taskReason = KubernetesUtility.getData(taskRunJson, "$.status.conditions[0].reason");
            System.out.println("Setting reason: " + taskReason);
            String taskMessage = KubernetesUtility.getData(taskRunJson, "$.status.conditions[0].message");
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
                logger.error("Error in getting steps. ", e);
                taskJson.put("steps", new JSONArray());
                continue;
            }

            System.out.println(steps.length());
            if ("fetch-source-code".equalsIgnoreCase(taskBaseName)) {
                taskJson.put("order", 1);
                String commitId = KubernetesUtility.getData(taskRunJson, "$.status.taskResults[0].value");
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
                String taskResult = KubernetesUtility.getData(taskRunJson, "$.status.taskResults[0].value");
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
}
