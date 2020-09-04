package org.zigmoi.ketchup.common;


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
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KubernetesUtility {

    private final static Logger logger = LoggerFactory.getLogger(KubernetesUtility.class);

    public static void main(String[] args) throws IOException, ApiException {
        //create pipeline resources in order. (createPipelineRun should be last.)
        String baseResourcePath = ConfigUtility.instance().getProperty("ketchup.test.default-pipeline-config");
        createCustomResource(baseResourcePath.concat("resource.yaml"), "default", "tekton.dev", "v1alpha1", "pipelineresources", "false");
        createCustomResource(baseResourcePath.concat("task-makisu.yaml"), "default", "tekton.dev", "v1alpha1", "tasks", "false");
        createCustomResource(baseResourcePath.concat("task-helm.yaml"), "default", "tekton.dev", "v1alpha1", "tasks", "false");
        createCustomResource(baseResourcePath.concat("pipeline.yaml"), "default", "tekton.dev", "v1alpha1", "pipelines", "false");
        createSecret(baseResourcePath.concat("secrets.yaml"));
        createServiceAccount(baseResourcePath.concat("service-account.yaml"));

        createCustomResource(baseResourcePath.concat("pipeline-run.yaml"), "default", "tekton.dev", "v1alpha1", "pipelineruns", "false");

//        createPipelineResource(baseResourcePath.concat("resource.yaml"));
//        createPipelineTask(baseResourcePath.concat("task-makisu.yaml"));
//        createPipelineTask(baseResourcePath.concat("task-helm.yaml"));
//        createSecret(baseResourcePath.concat("secrets.yaml"));
//        createServiceAccount(baseResourcePath.concat("service-account.yaml"));
//        createPipeline(baseResourcePath.concat("pipeline.yaml"));
//        createPipelineRun(baseResourcePath.concat("pipeline-run.yaml"));

        //  watchPipelineRunStatus();
        //  watchListPods();
        // getPodLogs("default", "demo-pipeline-run-1-build-image-lfkqj-pod-zfrfg", "step-build-and-push");
    }

    public static void createSecretUsingYamlContent(String resourceContent, String namespace, String pretty, String kubeConfig) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(IOUtils.toInputStream(kubeConfig, Charset.defaultCharset()));
//        client.setWriteTimeout(15000);
//        client.setConnectTimeout(15000);
//        client.setReadTimeout(15000);
        Configuration.setDefaultApiClient(client);

        V1Secret resource = (V1Secret) Yaml.load(resourceContent);
        CoreV1Api api = new CoreV1Api();
        Object result = api.createNamespacedSecret(namespace, resource, pretty, null, null);
        System.out.println(result);
    }

    public static void createServiceAccountUsingYamlContent(String resourceContent, String namespace, String pretty, String kubeConfig) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(IOUtils.toInputStream(kubeConfig, Charset.defaultCharset()));
//        client.setWriteTimeout(15000);
//        client.setConnectTimeout(15000);
//        client.setReadTimeout(15000);
        Configuration.setDefaultApiClient(client);

        V1ServiceAccount resource = (V1ServiceAccount) Yaml.load(resourceContent);
        CoreV1Api api = new CoreV1Api();

        Object result = api.createNamespacedServiceAccount(namespace, resource, pretty, null, null);
        System.out.println(result);
    }

    public static void createConfigmapUsingYamlContent(String resourceContent, String namespace, String pretty, String kubeConfig) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(IOUtils.toInputStream(kubeConfig, Charset.defaultCharset()));
        Configuration.setDefaultApiClient(client);

        V1ConfigMap resource = (V1ConfigMap) Yaml.load(resourceContent);
        CoreV1Api api = new CoreV1Api();

        Object result = api.createNamespacedConfigMap(namespace, resource, pretty, null, null);
    }

    public static String createSecret(String resourceFilePath) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
        Configuration.setDefaultApiClient(client);

        String namespace = "default";
        String pretty = "false";

        V1Secret resource = (V1Secret) Yaml.load(new File(resourceFilePath));
        CoreV1Api api = new CoreV1Api();

        Object result = api.createNamespacedSecret(namespace, resource, pretty, null, null);
        String responseJson = new Gson().toJson(result);
        return getData(responseJson, "$.metadata.name");
    }

    public static String createServiceAccount(String resourceFilePath) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
        Configuration.setDefaultApiClient(client);

        String namespace = "default";
        String pretty = "false";

        V1ServiceAccount resource = (V1ServiceAccount) Yaml.load(new File(resourceFilePath));
        CoreV1Api api = new CoreV1Api();

//        try {
        Object result = api.createNamespacedServiceAccount(namespace, resource, pretty, null, null);
        System.out.println(result);
        String responseJson = new Gson().toJson(result);
        return getData(responseJson, "$.metadata.name");
//        } catch (ApiException e) {
//            System.err.println("Exception when calling create secret.");
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Reason: " + e.getResponseBody());
//            System.err.println("Response headers: " + e.getResponseHeaders());
//            e.printStackTrace();
//        }
    }

    public static void watchAndStreamPipelineRunStatus(String kubeConfig, String namespace, String pipelineRunName, SseEmitter emitter) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
//        ApiClient client = Config.fromConfig(IOUtils.toInputStream(kubeConfig, Charset.defaultCharset()));
        Configuration.setDefaultApiClient(client);


        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelineruns"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String fieldSelector = "metadata.name=".concat(pipelineRunName);

        OkHttpClient httpClient =
                client.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        client.setHttpClient(httpClient);

        Watch<Object> watch =
                Watch.createWatch(
                        client,
                        apiInstance.listNamespacedCustomObjectCall(group, version, namespace,
                                plural, null, null, fieldSelector, null,
                                5, null, 30, true, null),
                        new TypeToken<Watch.Response<Object>>() {
                        }.getType());

        try {
            for (Watch.Response<Object> item : watch) {
                String responseJson = new Gson().toJson(item.object);
                System.out.println(responseJson);
                SseEmitter.SseEventBuilder eventBuilderDataStream = SseEmitter.event().name("data").data(parsePipelineRunResponse(responseJson).toString());
                emitter.send(eventBuilderDataStream);
                //  System.out.printf("%s : %s%n", item.type, item.object.toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            watch.close();
            SseEmitter.SseEventBuilder eventBuilderCloseStream = SseEmitter.event().data("").name("close");
            emitter.send(eventBuilderCloseStream);
            emitter.complete();
        }
    }

    public String getResourceNameUsingResourceYamlPath(String resourceFilePath) throws IOException {
        LinkedHashMap<String, Object> resource =
                (LinkedHashMap<String, Object>) Yaml.loadAs(new File(resourceFilePath), Map.class);
        String resourceJson = new Gson().toJson(resource);
        System.out.println(resourceJson);
        String resourceName = getData(resourceJson, "$.metadata.name");
        return resourceName;
    }

    public String getResourceNameUsingResourceJson(String resourceJson) {
        System.out.println(resourceJson);
        String resourceName = getData(resourceJson, "$.metadata.name");
        return resourceName;
    }

    public String getResourceNameUsingResourceYaml(String resourceYaml) {
        LinkedHashMap<String, Object> resource =
                (LinkedHashMap<String, Object>) Yaml.loadAs(resourceYaml, Map.class);
        String resourceJson = new Gson().toJson(resource);
        System.out.println(resourceJson);
        String resourceName = getData(resourceJson, "$.metadata.name");
        return resourceName;
    }

    public static void createCustomResource(String resourceFilePath, String namespace, String group, String version, String plural, String pretty) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
        Configuration.setDefaultApiClient(client);
        CustomObjectsApi apiInstance = new CustomObjectsApi(client);

        LinkedHashMap<String, Object> resource = loadYamlResourceAsMap(resourceFilePath);
        Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
        System.out.println(result);
    }

    public static void createCRDUsingYamlContent(String resourceYaml, String namespace, String group, String version, String plural, String pretty, String kubeConfig) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(IOUtils.toInputStream(kubeConfig, Charset.defaultCharset()));
//        client.setWriteTimeout(15000);
//        client.setConnectTimeout(15000);
//        client.setReadTimeout(15000);
        Configuration.setDefaultApiClient(client);
        CustomObjectsApi apiInstance = new CustomObjectsApi(client);

        LinkedHashMap<String, Object> resource = (LinkedHashMap<String, Object>) Yaml.loadAs(resourceYaml, Map.class);
        Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
        System.out.println(result);
    }

    public static String createPipelineResource(String resourceFilePath) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String namespace = "default";
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelineresources"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String pretty = "false";

        LinkedHashMap<String, Object> resource = loadYamlResourceAsMap(resourceFilePath);

//        try {
        Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
        System.out.println(result);
        String responseJson = new Gson().toJson(result);
        return getData(responseJson, "$.metadata.name");
//        } catch (ApiException e) {
//            System.err.println("Exception when calling create pipeline resource.");
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Reason: " + e.getResponseBody());
//            System.err.println("Response headers: " + e.getResponseHeaders());
//            e.printStackTrace();
//        }
    }

    public static String createPipelineTask(String resourceFilePath) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String namespace = "default";
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "tasks"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String pretty = "false";

        LinkedHashMap<String, Object> resource = loadYamlResourceAsMap(resourceFilePath);
        String rJson = new Gson().toJson(resource);
        System.out.println(getData(rJson, "$.metadata.name"));
        System.out.println(rJson);

//        try {
        Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
        System.out.println(result);
        String responseJson = new Gson().toJson(result);
        return getData(responseJson, "$.metadata.name");
//        } catch (ApiException e) {
//            System.err.println("Exception when calling create pipeline task.");
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Reason: " + e.getResponseBody());
//            System.err.println("Response headers: " + e.getResponseHeaders());
//            e.printStackTrace();
//        }
    }


    public static String createPipeline(String resourceFilePath) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String namespace = "default";
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelines"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String pretty = "false";

        LinkedHashMap<String, Object> resource = loadYamlResourceAsMap(resourceFilePath);

//        try {
        Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
        System.out.println(result);
        String responseJson = new Gson().toJson(result);
        return getData(responseJson, "$.metadata.name");
//        } catch (ApiException e) {
//            System.err.println("Exception when calling create pipeline.");
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Reason: " + e.getResponseBody());
//            System.err.println("Response headers: " + e.getResponseHeaders());
//            e.printStackTrace();
//        }
    }

    public static String createPipelineRun(String resourceFilePath) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String namespace = "default";
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelineruns"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String pretty = "false";

        LinkedHashMap<String, Object> resource = loadYamlResourceAsMap(resourceFilePath);

//        try {
        Object result = apiInstance.createNamespacedCustomObject(group, version, namespace, plural, resource, pretty);
        System.out.println(result);
        String responseJson = new Gson().toJson(result);
        return getData(responseJson, "$.metadata.name");
//        } catch (ApiException e) {
//            System.err.println("Exception when calling create pipelinerun.");
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Reason: " + e.getResponseBody());
//            System.err.println("Response headers: " + e.getResponseHeaders());
//            e.printStackTrace();
//        }
    }

    private static LinkedHashMap<String, Object> loadYamlResourceAsMap(String filePath) throws IOException {
        return (LinkedHashMap<String, Object>) Yaml.loadAs(new File(filePath), Map.class);
    }

    public static void watchAndStreamPipelineRunStatus(String pipelineRunName, SseEmitter emitter) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
        Configuration.setDefaultApiClient(client);


        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelineruns"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String fieldSelector = "metadata.name=".concat(pipelineRunName);

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
                SseEmitter.SseEventBuilder eventBuilderDataStream = SseEmitter.event().name("data").data(parsePipelineRunResponse(responseJson).toString());
                emitter.send(eventBuilderDataStream);
                //  System.out.printf("%s : %s%n", item.type, item.object.toString());
            }
        } finally {
            watch.close();
            SseEmitter.SseEventBuilder eventBuilderCloseStream = SseEmitter.event().data("").name("close");
            emitter.send(eventBuilderCloseStream);
            emitter.complete();
        }
    }

    public static void watchPipelineRunStatus() throws IOException, ApiException {
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
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

    private static JSONObject parsePipelineRunResponse(String responseJson) {
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
            logger.error("exception in getting taskruns, ", e);
            details.put("tasks", taskDetails);
            System.out.println("Details: " + details);
            return details;
        }

        for (Map.Entry<String, Object> tr : taskRuns.entrySet()) {
            String taskName = tr.getKey();
            String taskRunJson = new Gson().toJson(tr.getValue());
            String taskBaseName = getData(taskRunJson, "$.pipelineTaskName");
            String podName = getData(taskRunJson, "$.status.podName");
            String taskStartTime = getData(taskRunJson, "$.status.startTime");
            String taskCompletionTime = getData(taskRunJson, "$.status.completionTime");
            String taskStatus = getData(responseJson, "$.status.conditions[0].status");
            String taskReason = getData(responseJson, "$.status.conditions[0].reason");
            String taskMessage = getData(responseJson, "$.status.conditions[0].message");

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
            if ("build-image".equalsIgnoreCase(taskBaseName)) {
                JSONArray stepDetails = new JSONArray();
                for (Object stepEntry : steps) {
                    JSONObject step = (JSONObject) stepEntry;
                    String stepName = step.getString("name");
//                    if ("get-image-registry-config".equalsIgnoreCase(stepName)) {
//                        int order = 1;
//                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
//                    } else
                    if (stepName.startsWith("git-source-")) {
                        //git pull image name is dynamic, hence using prefix to match it.
                        int order = 1;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    } else if ("build-and-push".equalsIgnoreCase(stepName)) {
                        int order = 2;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    }
                }
                taskJson.put("steps", stepDetails);
            } else if ("deploy-chart-in-cluster".equalsIgnoreCase(taskBaseName)) {
                JSONArray stepDetails = new JSONArray();
                for (Object stepEntry : steps) {
                    JSONObject step = (JSONObject) stepEntry;
                    String stepName = step.getString("name");
//                    if ("get-kubeconfig".equalsIgnoreCase(stepName)) {
//                        int order = 1;
//                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
//                    }
//                    else if ("get-helm-chart".equalsIgnoreCase(stepName)) {
//                        int order = 2;
//                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
//                    }

                    if ("install-app-in-cluster".equalsIgnoreCase(stepName)) {
                        int order = 1;
                        stepDetails.put(parseStepDetails(tr, taskBaseName, podName, step, stepName, order));
                    }
                }
                taskJson.put("steps", stepDetails);
            }
            taskDetails.put(taskJson);
        }
        //details.put("steps", stepDetails);
        details.put("tasks", taskDetails);
        System.out.println("Details: " + details);
        return details;

    }

    private static String getData(String inputJson, String jsonPath) {
        String response = "";
        try {
            response = JsonPath.read(inputJson, jsonPath);
        } catch (Exception e) {
            logger.error("Exception in reading value at specified json path not found. ", e);
        }
        return response;
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
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
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
        ApiClient client = Config.fromConfig(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig"));
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

    public static InputStream getPodLogs(String kubeConfig, String namespace, String podName, String containerName, Integer tailLines) throws IOException, ApiException {
        try {
            ApiClient client = Config.fromConfig(IOUtils.toInputStream(kubeConfig, Charset.defaultCharset()));
            client.setReadTimeout(300_000);
            Configuration.setDefaultApiClient(client);
            PodLogs logs = new PodLogs();
            return logs.streamNamespacedPodLog(namespace, podName, containerName, null, tailLines, true);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw e;
        }
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
