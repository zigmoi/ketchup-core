package org.zigmoi.ketchup.deployment.controllers;

import com.google.common.io.ByteStreams;
import io.kubernetes.client.openapi.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentRequestDto;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentResponseDto;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.services.DeploymentService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class DeploymentController {
    private ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    @Autowired
    private DeploymentService deploymentService;

    @GetMapping("/public/sse")
    public SseEmitter handleSse(@RequestParam("pipelineRunName") String pipelineRunName) {
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.execute(() -> {
            try {
                KubernetesUtility.watchAndStreamPipelineRunStatus(pipelineRunName, emitter);
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @GetMapping("public/deployment/logs")
    public ResponseEntity<StreamingResponseBody> streamPipelineLogs(@RequestParam("podName") String podName,@RequestParam("containerName") String containerName) throws IOException, ApiException {
       InputStream logStream =  KubernetesUtility.getPodLogs("default", podName, containerName);
        StreamingResponseBody stream = out -> {
            ByteStreams.copy(logStream, out);
        };
        return new ResponseEntity(stream, HttpStatus.OK);
    }

    @PutMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}/status/{status}")
    public void updateDeploymentStatus(@PathVariable("status") String status, @PathVariable String deploymentResourceId, @PathVariable String projectResourceId) {
        deploymentService.updateDeploymentStatus(projectResourceId, deploymentResourceId, status);
    }

    @PutMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}/displayName/{displayName}")
    public void updateDeploymentDisplayName(@PathVariable("deploymentResourceId") String deploymentResourceId, @PathVariable("displayName") String displayName, @PathVariable String projectResourceId) {
        deploymentService.updateDeploymentDisplayName(projectResourceId, deploymentResourceId, displayName);
    }

    @PostMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot")
    public void createBasicSpringBootDeployment(@RequestBody BasicSpringBootDeploymentRequestDto basicSpringBootDeploymentRequestDto, @PathVariable String projectResourceId) {
        deploymentService.createBasicSpringBootDeployment(projectResourceId, basicSpringBootDeploymentRequestDto);
    }

    @GetMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot/{deploymentResourceId}")
    public BasicSpringBootDeploymentResponseDto getBasicSpringBootDeployment(@PathVariable String projectResourceId, @PathVariable String deploymentResourceId) {
        return deploymentService.getBasicSpringBootDeployment(projectResourceId, deploymentResourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        String.format("Deployment with id %s not found.", deploymentResourceId)));
    }

    @DeleteMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}")
    public void deleteDeployment(@PathVariable String projectResourceId, @PathVariable String deploymentResourceId) {
        deploymentService.deleteDeployment(projectResourceId, deploymentResourceId);
    }

    @GetMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot/list")
    public List<DeploymentEntity> listAllBasicSpringBootDeployments(@PathVariable String projectResourceId) {
        return deploymentService.listAllBasicSpringBootDeployments(projectResourceId);
    }

}
