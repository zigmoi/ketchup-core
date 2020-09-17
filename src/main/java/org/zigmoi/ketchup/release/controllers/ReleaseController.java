package org.zigmoi.ketchup.release.controllers;

import com.google.common.io.ByteStreams;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.deployment.dtos.DeploymentDetailsDto;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.services.ReleaseService;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
public class ReleaseController {

    private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    @Autowired
    private ReleaseService releaseService;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostMapping("/v1/release")
    public void createRelease(@RequestParam("deploymentId") String deploymentResourceId) {
        releaseService.create(deploymentResourceId);
    }

    @GetMapping("/v1/release")
    public Release getRelease(@RequestParam("releaseResourceId") String releaseResourceId) {
        return releaseService.findById(releaseResourceId);
    }

    @GetMapping("/v1/release/stop")
    public void stopRelease(@RequestParam("releaseResourceId") String releaseResourceId) {
        releaseService.stop(releaseResourceId);
    }

    @GetMapping("/v1/releases")
    public Set<Release> listAllReleasesInDeployment(@RequestParam("deploymentId") String deploymentResourceId) {
        return releaseService.listAllInDeployment(deploymentResourceId);
    }

    @GetMapping("/v1/release/pipeline/status/stream/sse")
    public SseEmitter streamPipelineStatus(@RequestParam("releaseId") String releaseResourceId) {
        Release release = releaseService.findById(releaseResourceId);
        String kubeConfig = getKubeConfig(release.getDeploymentDataJson());
        String namespace = getKubernetesNamespace(release.getDeploymentDataJson());
        SseEmitter emitter = new SseEmitter();
        if ("SUCCESS".equalsIgnoreCase(release.getStatus()) || "FAILED".equalsIgnoreCase(release.getStatus())) {
            nonBlockingService.execute(() -> {
                try {
                    SseEmitter.SseEventBuilder eventBuilderDataStream =
                            SseEmitter.event()
                                    .name("data")
                                    .reconnectTime(10_000)
                                    .data(release.getPipelineStatusJson());
                    emitter.send(eventBuilderDataStream);
                    SseEmitter.SseEventBuilder eventBuilderCloseStream =
                            SseEmitter.event()
                                    .name("close")
                                    .data("");
                    emitter.send(eventBuilderCloseStream);
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            });
        } else {
            String pipelineRunName = "pipeline-run-".concat(releaseResourceId);
            nonBlockingService.execute(() -> {
                try {
                    KubernetesUtility.watchAndStreamPipelineRunStatus(kubeConfig, namespace, pipelineRunName, emitter);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                    log.error(ex.getLocalizedMessage(), ex);
                }
            });
        }
        return emitter;
    }

    private String getKubeConfig(String deploymentDataJson) {
        JSONObject jo = new JSONObject(deploymentDataJson);
        return StringUtility.decodeBase64((jo.getString("devKubeconfig")));
    }

    private String getKubernetesNamespace(String deploymentDataJson) {
        JSONObject jo = new JSONObject(deploymentDataJson);
        return jo.getString("devKubernetesNamespace");
    }

    private InputStream getLogsInputStream(String releaseResourceId, String podName, String containerName, Integer tailLines) throws IOException, ApiException {
        Release release = releaseService.findById(releaseResourceId);
        DeploymentDetailsDto deploymentDetailsDto = releaseService.extractDeployment(release);
        return KubernetesUtility.getPodLogs(StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig()),
                deploymentDetailsDto.getDevKubernetesNamespace(), podName, containerName, tailLines);
    }

    //    @GetMapping(value = "/v1/release/pipeline/pod-container/logs/stream/direct")
    @GetMapping(value = "/v1/release/pipeline/logs/stream/direct")
    public void streamPipelineLogsDirect(HttpServletResponse response,
                                         @RequestParam("releaseId") String releaseResourceId,
                                         @RequestParam("podName") String podName,
                                         @RequestParam("containerName") String containerName,
                                         @RequestParam(value = "tailLines", required = false) Integer tailLines) throws IOException, ApiException {
        try (InputStream logStream = getLogsInputStream(releaseResourceId, podName, containerName, tailLines)) {
            //noinspection UnstableApiUsage
            ByteStreams.copy(logStream, response.getOutputStream());
        }
    }

    //    @GetMapping(value = "/v1/release/pipeline/pod-container/logs/stream/sse")
    @GetMapping(value = "/v1/release/pipeline/logs/stream/sse")
    public SseEmitter streamPipelineLogsSSE(@RequestParam("releaseId") String releaseResourceId,
                                            @RequestParam("podName") String podName,
                                            @RequestParam("containerName") String containerName,
                                            @RequestParam(value = "tailLines", required = false) Integer tailLines) {
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getLogsInputStream(releaseResourceId, podName, containerName, tailLines)))) {
                String response;
                while ((response = reader.readLine()) != null) {
                    SseEmitter.SseEventBuilder eventBuilderDataStream = SseEmitter.event().data(response, MediaType.TEXT_PLAIN);
                    emitter.send(eventBuilderDataStream);
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                emitter.complete();
            }
        });
        return emitter;
    }
}
