package org.zigmoi.ketchup.common;


import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.util.Yaml;
import okhttp3.OkHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KubernetesUtility {

    private final static Logger logger = LoggerFactory.getLogger(KubernetesUtility.class);

    public static void main(String[] args) throws IOException, ApiException {
        //create pipeline resources in order. (createPipelineRun should be last.)
        createPipelineResource("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/resource.yaml");
        createPipelineTask("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/task-makisu.yaml");
        createPipelineTask("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/task-helm.yaml");
        createSecret("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/secrets.yaml");
        createServiceAccount("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/service-account.yaml");
        createPipeline("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/pipeline.yaml");
        createPipelineRun("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/pipeline-run.yaml");


       //  watchPipelineRunStatus();
        //  watchListPods();
    }

    public static void createSecret(String resourceFilePath) throws IOException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);

        String namespace = "default";
        String pretty = "false";

        V1Secret resource = (V1Secret) Yaml.load(new File(resourceFilePath));
        CoreV1Api api = new CoreV1Api();

        try {
            Object result = api.createNamespacedSecret(namespace, resource, pretty, null, null);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling create secret.");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }

    public static void createServiceAccount(String resourceFilePath) throws IOException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);

        String namespace = "default";
        String pretty = "false";

        V1ServiceAccount resource = (V1ServiceAccount) Yaml.load(new File(resourceFilePath));
        CoreV1Api api = new CoreV1Api();

        try {
            Object result = api.createNamespacedServiceAccount(namespace, resource, pretty, null, null);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling create secret.");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }

    public static void createPipelineResource(String resourceFilePath) throws IOException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String namespace = "default";
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelineresources"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String pretty = "false";

        LinkedHashMap<String, Object> resource = loadYamlResourceAsMap(resourceFilePath);

        try {
            Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling create pipeline resource.");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }

    public static void createPipelineTask(String resourceFilePath) throws IOException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String namespace = "default";
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "tasks"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String pretty = "false";

        LinkedHashMap<String, Object> resource = loadYamlResourceAsMap(resourceFilePath);

        try {
            Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling create pipeline task.");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }


    public static void createPipeline(String resourceFilePath) throws IOException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String namespace = "default";
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelines"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String pretty = "false";

        LinkedHashMap<String, Object> resource = loadYamlResourceAsMap(resourceFilePath);

        try {
            Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling create pipeline.");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }

    public static void createPipelineRun(String resourceFilePath) throws IOException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String namespace = "default";
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelineruns"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String pretty = "false";

        LinkedHashMap<String, Object> resource = loadYamlResourceAsMap(resourceFilePath);

        try {
            Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling create pipelinerun.");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }

    private static LinkedHashMap<String, Object> loadYamlResourceAsMap(String filePath) throws IOException {
        return (LinkedHashMap<String, Object>) Yaml.loadAs(new File(filePath), Map.class);
    }

    public static void watchPipelineRunStatus() throws IOException, ApiException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);


        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelineruns"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String name = "demo-pipeline-run-1"; // String | the custom object's name
        String fieldSelector = "metadata.name=demo-pipeline-run-1";

        OkHttpClient httpClient =
                client.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        client.setHttpClient(httpClient);

        Watch<Object> watch =
                Watch.createWatch(
                        client,
                        apiInstance.listNamespacedCustomObjectCall(group, version, "default",
                                plural, null, null, fieldSelector, null,
                                5, null, 75, true, null),
                        new TypeToken<Watch.Response<Object>>() {
                        }.getType());

        try {
            for (Watch.Response<Object> item : watch) {
                String responseJson = new Gson().toJson(item.object);
                System.out.println(responseJson);
                parsePipelineRunResponse(responseJson);
                //  System.out.printf("%s : %s%n", item.type, item.object.toString());
            }
        } finally {
            watch.close();
        }
    }

    private static void parsePipelineRunResponse(String responseJson) {
        JSONObject details = new JSONObject();
        String startTime = JsonPath.read(responseJson, "$.status.startTime");
        details.put("startTime", startTime);
        String completionTime = JsonPath.read(responseJson, "$.status.completionTime");
        details.put("completionTime", completionTime);
        String status = JsonPath.read(responseJson, "$.status.conditions[0].status");
        details.put("status", status);
        String reason = JsonPath.read(responseJson, "$.status.conditions[0].reason");
        details.put("reason", reason);
        String message = JsonPath.read(responseJson, "$.status.conditions[0].message");
        details.put("message", message);

        JSONArray stepDetails = new JSONArray();
        LinkedHashMap<String, Object> taskRuns = JsonPath.read(responseJson, "$.status.taskRuns");
        for (Map.Entry<String, Object> tr : taskRuns.entrySet()) {
            String taskRunJson = new Gson().toJson(tr.getValue());
            String taskBaseName = JsonPath.read(taskRunJson, "$.pipelineTaskName");
            String podName = JsonPath.read(taskRunJson, "$.status.podName");
            JSONArray steps = new JSONArray(JsonPath.read(taskRunJson, "$.status.steps").toString());
            System.out.println(steps.length());
            if ("build-image".equalsIgnoreCase(taskBaseName)) {
                for (Object stepEntry : steps) {
                    JSONObject step = (JSONObject) stepEntry;
                    String stepName = step.getString("name");
                    if ("get-image-registry-config".equalsIgnoreCase(stepName)) {
                        int order = 1;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    } else if (stepName.startsWith("git-source-")) {
                        //git pull image name is dynamic, hence using prefix to match it.
                        int order = 2;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    } else if ("build-and-push".equalsIgnoreCase(stepName)) {
                        int order = 3;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    }
                }
            } else if ("deploy-chart-in-cluster".equalsIgnoreCase(taskBaseName)) {
                for (Object stepEntry : steps) {
                    JSONObject step = (JSONObject) stepEntry;
                    String stepName = step.getString("name");
                    if ("get-kubeconfig".equalsIgnoreCase(stepName)) {
                        int order = 4;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    } else if ("get-helm-chart".equalsIgnoreCase(stepName)) {
                        int order = 5;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    } else if ("install-app-in-cluster".equalsIgnoreCase(stepName)) {
                        int order = 6;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    }
                }
            }
        }
        details.put("steps", stepDetails);
        System.out.println("Details: " + details);

    }

    private static JSONObject parseStepDetails(Map.Entry<String, Object> tr, String taskBaseName, String podName, JSONObject step, String stepName, int order) {
        JSONObject stepJson = new JSONObject();
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
            String stepCompletionTime = step.getJSONObject("terminated").getString("finishedAt");
            stepJson.put("completionTime", stepCompletionTime);
            int stepExitCode = step.getJSONObject("terminated").getInt("exitCode");
            stepJson.put("exitCode", stepExitCode);
            if (stepExitCode == 0) {
                stepJson.put("status", true);
            } else {
                stepJson.put("status", false);
            }
        }
        return stepJson;
    }

    public static void watchListPods() throws IOException, ApiException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);

        OkHttpClient httpClient =
                client.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        client.setHttpClient(httpClient);

        CoreV1Api coreApi = new CoreV1Api();
        Watch<V1Pod> watch =
                Watch.createWatch(
                        client,
                        coreApi.listNamespacedPodCall("default", "false", false,
                                null, "metadata.name=ketchup-sb-demo1-basic-springboot-demo-ketchup-5c7f87c866-cs4xp", null, 5, null,
                                30, true, null),
                        new TypeToken<Watch.Response<V1Pod>>() {
                        }.getType());

        try {
            for (Watch.Response<V1Pod> item : watch) {
                System.out.printf("%s : %s%n", item.type, item.object.toString());
            }
        } finally {
            watch.close();
        }
    }


    public static void getPipeLineRunDetails() throws IOException, ApiException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);
        CoreV1Api coreApi = new CoreV1Api(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelineruns"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String name = "demo-pipeline-run-1"; // String | the custom object's name
        try {
            Object result = apiInstance.getNamespacedCustomObjectStatus(group, version, "default", plural, name);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CustomObjectsApi#getClusterCustomObject");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }

    }

    public static void getPodLogs() throws IOException, ApiException {
        ApiClient client = Config.fromConfig("/Users/neo/Documents/dev/java/ketchup-demo-basicspringboot/standard-tkn-pipeline1-cloud/kubeconfig");
        Configuration.setDefaultApiClient(client);
        CoreV1Api coreApi = new CoreV1Api(client);

        V1Pod pod = coreApi.listNamespacedPod("default", "false", null, null, null, null, null, null, null, null)
                .getItems()
                .get(0);
        System.out.println(pod.getMetadata().getName());

        PodLogs logs = new PodLogs();
        InputStream is = logs.streamNamespacedPodLog(pod);
        //  InputStream is = logs.streamNamespacedPodLog("default", "demo-pipeline-run-1-build-image-h7pc5-pod-hpg7k", "step-build-and-push");
        ByteStreams.copy(is, System.out);

    }

    public static void createDeploymentInAws(File kubeConfig, String namespace, String appId, String awsEcrImageLink, int port,
                                             Map<String, List<String>> hostnameAlias, boolean updateIfAlreadyExists, String deploymentName) throws IOException, ApiException {

        // metadata
        V1ObjectMeta deploymentMetadata = new V1ObjectMeta()
                .name(deploymentName)
                .namespace(namespace)
                .putLabelsItem("app", appId);

        V1DeploymentSpec v1DeploymentSpec = getDeploymentSpec(namespace, appId, awsEcrImageLink, port, hostnameAlias);

        // L0
        V1Deployment deployment = new V1DeploymentBuilder()
                .withMetadata(deploymentMetadata)
                .withSpec(v1DeploymentSpec)
                .build();

        logger.info(new JSONObject(deployment).toString(4));

        // Call API
        ApiClient client = Config.fromConfig(kubeConfig.getAbsolutePath());
        AppsV1Api appsV1Api = new AppsV1Api(client);

        if (deploymentAlreadyExists(appsV1Api, appId, namespace, deploymentName) && updateIfAlreadyExists) {
            List<V1Deployment> deployments = new ArrayList<>();
            appsV1Api.patchNamespacedDeployment(deploymentName, namespace, deployments, null, null, null, false);
        } else {
            appsV1Api.createNamespacedDeployment(namespace, deployment, null, null, null);
        }
    }

    public static boolean deploymentAlreadyExists(File kubeConfig, String appId, String namespace, String deploymentName) throws ApiException, IOException {
        ApiClient client = Config.fromConfig(kubeConfig.getAbsolutePath());
        AppsV1Api appsV1Api = new AppsV1Api(client);
        return deploymentAlreadyExists(appsV1Api, appId, namespace, deploymentName);
    }

    private static boolean deploymentAlreadyExists(AppsV1Api appsV1Api, String appId, String namespace, String deploymentName) throws ApiException {
        String label = "app=" + appId;
        V1DeploymentList deploymentList = appsV1Api.listNamespacedDeployment(namespace, "true", null, null,
                null, label, null, null, -1, false);
        if (deploymentList == null || deploymentList.getItems().isEmpty()) {
            return false;
        }
        for (V1Deployment deployment : deploymentList.getItems()) {
            if (deploymentName.equals(deployment.getMetadata().getName())) {
                return true;
            }
        }
        return false;
    }

    private static V1DeploymentSpec getDeploymentSpec(String namespace, String appId, String awsEcrImageLink, int port,
                                                      Map<String, List<String>> hostnameAlias) {

        // spec
        V1PodSpec v1PodSpec = new V1PodSpec()
                .addContainersItem(new V1Container()
                        .image(awsEcrImageLink)
                        .imagePullPolicy("Always")
                        .name(appId)
                        .addPortsItem(new V1ContainerPort().containerPort(port)));

        if (hostnameAlias != null) {
            for (Map.Entry<String, List<String>> entry : hostnameAlias.entrySet()) {
                V1HostAlias hostAlias = new V1HostAlias();
                for (String hostname : entry.getValue()) {
                    hostAlias.addHostnamesItem(hostname);
                }
                hostAlias.ip(entry.getKey());
                v1PodSpec.addHostAliasesItem(hostAlias);
            }
        }

        return new V1DeploymentSpec()
                .selector(new V1LabelSelector()
                        .putMatchLabelsItem("app", appId))
                .template(new V1PodTemplateSpec()
                        .metadata(new V1ObjectMeta()
                                .namespace(namespace)
                                .putLabelsItem("app", appId))
                        .spec(v1PodSpec));
    }
}
