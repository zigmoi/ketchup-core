package org.zigmoi.ketchup.test;

import com.google.common.io.ByteStreams;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.io.InputStream;

public class KubernetesLogsExample {
    public static void main(String[] args) throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        CoreV1Api coreApi = new CoreV1Api(client);

        PodLogs logs = new PodLogs();
        V1Pod pod = coreApi
                .listNamespacedPod("default", "false", null, null, null, null, null, null, null, null)
                .getItems()
                .get(0);

        InputStream is = logs.streamNamespacedPodLog(pod);
        ByteStreams.copy(is, System.out);
    }
}
