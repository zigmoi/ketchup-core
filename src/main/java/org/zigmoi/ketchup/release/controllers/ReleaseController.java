package org.zigmoi.ketchup.release.controllers;

import com.google.common.io.ByteStreams;
import io.kubernetes.client.openapi.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.services.ReleaseService;

import java.io.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class ReleaseController {
    private ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    @Autowired
    private ReleaseService releaseService;

    @PostMapping("/v1/release")
    public void createRelease(@RequestParam("deploymentId") String deploymentResourceId) {
        releaseService.create(deploymentResourceId);
    }

    @GetMapping("/v1/release")
    public Release getRelease(@RequestParam("releaseResourceId") String releaseResourceId) {
        return releaseService.findById(releaseResourceId);
    }

    @GetMapping("/v1/releases")
    public Set<Release> listAllReleasesInDeployment(@RequestParam("deploymentId") String deploymentResourceId) {
        return releaseService.listAllInDeployment(deploymentResourceId);
    }

    @GetMapping("/v1/release/pipeline/status/stream/sse")
    public SseEmitter streamPipelineStatus(@RequestParam("releaseId") String releaseResourceId) {
        Release release = releaseService.findById(releaseResourceId);
        SseEmitter emitter = new SseEmitter();
        if ("SUCCESS".equalsIgnoreCase(release.getStatus()) || "FAILED".equalsIgnoreCase(release.getStatus())) {
            nonBlockingService.execute(() -> {
                try {
                    SseEmitter.SseEventBuilder eventBuilderDataStream =
                            SseEmitter.event()
                                    .name("data")
                                    .reconnectTime(10000)
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
            String pipelineRunName = "demo-pipeline-run-1";
            nonBlockingService.execute(() -> {
                try {
                    KubernetesUtility.watchAndStreamPipelineRunStatus(pipelineRunName, emitter);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            });
        }
        return emitter;
    }

    @GetMapping(value = "/v1/release/pipeline/logs/stream/direct")
    public ResponseEntity<StreamingResponseBody> streamPipelineLogs(@RequestParam("releaseId") String releaseResourceId, @RequestParam("podName") String podName, @RequestParam("containerName") String containerName) throws IOException, ApiException {
        // Release release = releaseService.findById(releaseResourceId);

        String namespace = "default";
//        String podName = "demo-pipeline-run-1-build-image-jtw2m-pod-d28mc";
//        String containerName = "step-build-and-push";
        InputStream logStream = KubernetesUtility.getPodLogs(namespace, podName, containerName);
        StreamingResponseBody stream = out -> {
            ByteStreams.copy(logStream, out);
        };
        return new ResponseEntity(stream, HttpStatus.OK);
    }

    @GetMapping("/v1/release/pipeline/logs/stream/sse")
    public SseEmitter streamPipelineLogsSse(@RequestParam("releaseId") String releaseResourceId, @RequestParam("podName") String podName, @RequestParam("containerName") String containerName) {
        // Release release = releaseService.findById(releaseResourceId);
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.execute(() -> {
            try {
                String namespace = "default";
                InputStream inputStream = KubernetesUtility.getPodLogs(namespace, podName, containerName);

                //creating an InputStreamReader object
                InputStreamReader isReader = new InputStreamReader(inputStream);
                //Creating a BufferedReader object
                BufferedReader reader = new BufferedReader(isReader);
                StringBuffer sb = new StringBuffer();
                String str;
                while ((str = reader.readLine()) != null) {
                    System.out.println(str);
                    SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().data(str).name("data");
                    emitter.send(eventBuilder);
                }
                SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event().data("").name("close");
                emitter.send(eventBuilder);
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

}
