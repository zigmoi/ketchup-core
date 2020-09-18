package org.zigmoi.ketchup.deployment.controllers;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.deployment.DeploymentConstants;
import org.zigmoi.ketchup.deployment.dtos.DeploymentDetailsDto;
import org.zigmoi.ketchup.deployment.dtos.DeploymentRequestDto;
import org.zigmoi.ketchup.deployment.dtos.DeploymentResponseDto;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.services.DeploymentService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DeploymentController {

    @Autowired
    private DeploymentService deploymentService;

    @GetMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}/instances")
    public List<String> getDeploymentInstances(@PathVariable String deploymentResourceId) {
        //TODO find active version of deployment and use its kubeconfig to get instances.
        DeploymentDetailsDto deploymentDetailsDto = deploymentService.getDeployment(deploymentResourceId);
        String kubeConfig = StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig());
        try {
            String labelSelector = "app.kubernetes.io/instance=release-".concat(deploymentResourceId);
            V1PodList res = KubernetesUtility.listPods(labelSelector, "default", "false", kubeConfig);
            System.out.println(res);
            return res.getItems().stream().map(pod -> pod.getMetadata().getName()).collect(Collectors.toList());
        } catch (IOException | ApiException e ) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get instances.");
        }
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
    public void createBasicSpringBootDeployment(@RequestBody DeploymentRequestDto deploymentRequestDto, @PathVariable String projectResourceId) {
        deploymentRequestDto.setApplicationType(DeploymentConstants.APP_TYPE_BASIC_SPRING_BOOT);
        deploymentService.createDeployment(projectResourceId, deploymentRequestDto);
    }

    @GetMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot/{deploymentResourceId}")
    public DeploymentResponseDto getBasicSpringBootDeployment(@PathVariable String projectResourceId, @PathVariable String deploymentResourceId) {
        return deploymentService.getDeploymentDetails(deploymentResourceId);
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
