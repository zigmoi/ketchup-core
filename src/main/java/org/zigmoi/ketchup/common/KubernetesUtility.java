package org.zigmoi.ketchup.common;


import com.google.common.io.ByteStreams;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KubernetesUtility {

    private final static Logger logger = LoggerFactory.getLogger(KubernetesUtility.class);

    public static void main(String[] args) throws IOException, ApiException {
        getPodLogs();
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
            appsV1Api.patchNamespacedDeployment(deploymentName, namespace, deployments, null, null,null ,false );
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
        String label = "app="+appId;
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
