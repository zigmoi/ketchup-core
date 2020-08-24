package org.zigmoi.ketchup.test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.zigmoi.ketchup.common.ConfigUtility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class KubernetesApiTest {

    public static void main(String[] args) throws IOException, ApiException {
        String content = FileUtils.readFileToString(new File(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig")), StandardCharsets.UTF_8);
//        String content = FileUtils.readFileToString(new File(ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig")), StandardCharsets.UTF_8);
        watchAndStreamPipelineRunStatus(content, "default", "pipeline-run-91a510b7-fac9-4c49-8cf5-90d72b40e280");
    }

    public static void watchAndStreamPipelineRunStatus(String kubeConfig, String namespace, String pipelineRunName) throws IOException, ApiException {
        ApiClient client = Config.fromConfig(IOUtils.toInputStream(kubeConfig, Charset.defaultCharset()));
        Configuration.setDefaultApiClient(client);

        CustomObjectsApi apiInstance = new CustomObjectsApi(client);
        String group = "tekton.dev"; // String | the custom resource's group
        String version = "v1alpha1"; // String | the custom resource's version
        String plural = "pipelineruns"; // String | the custom object's plural name. For TPRs this would be lowercase plural kind.
        String fieldSelector = "metadata.name=".concat(pipelineRunName);

        OkHttpClient httpClient = client.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        client.setHttpClient(httpClient);

        try (Watch<Object> watch = Watch.createWatch(
                client,
                apiInstance.listNamespacedCustomObjectCall(group, version, namespace,
                        plural, null, null, fieldSelector, null,
                        5, null, 30, true, null),
                new TypeToken<Watch.Response<Object>>() {
                }.getType())) {
            for (Watch.Response<Object> item : watch) {
                String responseJson = new Gson().toJson(item.object);
                System.out.println(responseJson);
                //  System.out.printf("%s : %s%n", item.type, item.object.toString());
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void main2(String[] args) throws IOException, ApiException {

//        CoreV1Api coreV1Api = new CoreV1Api(client);
//        V1Namespace ns2 = coreV1Api.readNamespace("ovacs-default", null, null, null);
//        System.out.println(new JSONObject(ns2).toString());

        String namespace = "ovacs-default";

        // metadata
        V1ObjectMeta deploymentMetadata = new V1ObjectMeta()
                .name("gridmaze-subscriber-app-deployment")
                .namespace(namespace)
                .putLabelsItem("app", "gridmaze-subscriber-app");

        // spec
        V1DeploymentSpec v1DeploymentSpec = new V1DeploymentSpec()
                .selector(new V1LabelSelector()
                        .putMatchLabelsItem("app", "gridmaze-subscriber-app"))
                .template(new V1PodTemplateSpec()
                        .metadata(new V1ObjectMeta()
                                .namespace(namespace)
                                .putLabelsItem("app", "gridmaze-subscriber-app"))
                        .spec(new V1PodSpec()
                                .addContainersItem(new V1Container()
                                        .image("936243011424.dkr.ecr.us-east-1.amazonaws.com/ovacs/gridmaze-subscriber-app:latest")
                                        .imagePullPolicy("Always")
                                        .name("gridmaze-subscriber-app")
                                        .addPortsItem(new V1ContainerPort().containerPort(8080)))
                                .addHostAliasesItem(new V1HostAlias()
                                        .addHostnamesItem("r1.ovacs.aws")
                                        .ip("172.16.62.64"))));

        // L0
        V1Deployment deployment = new V1DeploymentBuilder()
                .withMetadata(deploymentMetadata)
                .withSpec(v1DeploymentSpec)
                .build();

        System.out.println(new JSONObject(deployment).toString(4));

        // Call API
        ApiClient client = Config.fromConfig("/home/tapo/IdeaProjects/gamma-dev/gridmaze/gridmaze-cluster-admin/config/ovacs-kubeconfig.yaml");
        AppsV1Api appsV1Api = new AppsV1Api(client);
        appsV1Api.createNamespacedDeployment(namespace, deployment, null, null, null);
    }
}
