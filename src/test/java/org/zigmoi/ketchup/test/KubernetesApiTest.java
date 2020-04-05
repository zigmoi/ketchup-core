package org.zigmoi.ketchup.test;


import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import org.json.JSONObject;

import java.io.IOException;

public class KubernetesApiTest {

    public static void main(String[] args) throws IOException, ApiException {

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
