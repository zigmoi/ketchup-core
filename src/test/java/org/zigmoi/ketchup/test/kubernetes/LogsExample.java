package org.zigmoi.ketchup.test.kubernetes;

import com.google.common.io.ByteStreams;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import org.zigmoi.ketchup.common.ConfigUtility;
import org.zigmoi.ketchup.common.FileUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;

import java.io.IOException;
import java.io.InputStream;

public class LogsExample {

    public static void main(String[] args) throws IOException, ApiException, InterruptedException {
        String kubeConfigFile = ConfigUtility.instance().getProperty("ketchup.test.default-kubeconfig");
        ApiClient client = Config.fromConfig(kubeConfigFile);
        Configuration.setDefaultApiClient(client);
        CoreV1Api coreApi = new CoreV1Api(client);
        Configuration.setDefaultApiClient(client);

//    PodLogs logs = new PodLogs();
//    V1Pod pod =
//            coreApi
//                    .listNamespacedPod("default", "false", null, null, null, null, null, null, null, null)
//                    .getItems()
//                    .get(0);
//    default :: release-4015fe6c-0c58-4ce8-9213-2a1576f3752e-basic-springbznfjh :: basic-springboot-demo-ketchup;
        String namespace, podName, containerName;
        namespace = "default";
        podName = "release-4015fe6c-0c58-4ce8-9213-2a1576f3752e-basic-springbznfjh";
        containerName = "basic-springboot-demo-ketchup";
        PodLogs logs = new PodLogs();

        InputStream is = logs.streamNamespacedPodLog(namespace, podName, containerName);
        ByteStreams.copy(KubernetesUtility.getPodLogs(FileUtility.readDataFromFile(kubeConfigFile), namespace, podName, containerName, null), System.out);
        Thread.sleep(300000);
    }
}
