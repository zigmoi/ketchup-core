package org.zigmoi.ketchup.test;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.util.Config;
import org.json.JSONObject;

import java.io.IOException;

public class KubernetesApiTest {

    public static void main(String[] args) throws IOException, ApiException {
//        ApiClient client = Config.defaultClient();
        ApiClient client = Config.fromConfig("/home/tapo/IdeaProjects/gamma-dev/gridmaze/gridmaze-cluster-admin/config/ovacs-kubeconfig.yaml");
        CoreV1Api api = new CoreV1Api(client);
        V1Namespace ns2 = api.readNamespace("ovacs-default", null, null, null);
        System.out.println(new JSONObject(ns2).toString());
    }
}
