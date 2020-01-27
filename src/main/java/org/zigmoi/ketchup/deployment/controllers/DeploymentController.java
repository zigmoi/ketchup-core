package org.zigmoi.ketchup.deployment.controllers;

import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentRequestDto;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentResponseDto;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.services.DeploymentService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@RestController
public class DeploymentController {

    @Autowired
    private DeploymentService deploymentService;

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
