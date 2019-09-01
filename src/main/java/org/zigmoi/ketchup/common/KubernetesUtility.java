package org.zigmoi.ketchup.common;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class KubernetesUtility {

    public static void deployInAws(File kubeConfig, String namespace, String appId, String awsEcrImageLink, int port,
                                   Map<String, List<String>> hostnameAlias) throws IOException, ApiException {

        // metadata
        V1ObjectMeta deploymentMetadata = new V1ObjectMeta()
                .name(appId+"-deployment")
                .namespace(namespace)
                .putLabelsItem("app", appId);

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

        V1DeploymentSpec v1DeploymentSpec = new V1DeploymentSpec()
                .selector(new V1LabelSelector()
                        .putMatchLabelsItem("app", appId))
                .template(new V1PodTemplateSpec()
                        .metadata(new V1ObjectMeta()
                                .namespace(namespace)
                                .putLabelsItem("app", appId))
                        .spec(v1PodSpec));

        // L0
        V1Deployment deployment = new V1DeploymentBuilder()
                .withMetadata(deploymentMetadata)
                .withSpec(v1DeploymentSpec)
                .build();

        System.out.println(new JSONObject(deployment).toString(4));

        // Call API
        ApiClient client = Config.fromConfig(kubeConfig.getAbsolutePath());
        AppsV1Api appsV1Api = new AppsV1Api(client);
        appsV1Api.createNamespacedDeployment(namespace, deployment, null, null, null);
    }
}
