package org.zigmoi.ketchup.deployment.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentRequestDto;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentResponseDto;
import org.zigmoi.ketchup.deployment.services.DeploymentService;

import java.util.List;

@RestController
public class DeploymentController {

    @Autowired
    private DeploymentService deploymentService;

    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @PutMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}/status/{status}")
    public void updateDeploymentStatus(@PathVariable("status") String status, @PathVariable String deploymentResourceId, @PathVariable String projectResourceId) {
        deploymentService.updateDeploymentStatus(projectResourceId, deploymentResourceId, status);
    }

    @PreAuthorize("hasAnyRole('ROLE_PROJECT_MEMBER')")
    @PutMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}/displayName/{displayName}")
    public void updateDeploymentDisplayName(@PathVariable("deploymentResourceId") String deploymentResourceId, @PathVariable("displayName") String displayName, @PathVariable String projectResourceId) {
        deploymentService.updateDeploymentDisplayName(projectResourceId, deploymentResourceId, displayName);
    }

//    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @PostMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot")
    public void createBasicSpringBootDeployment(@RequestBody BasicSpringBootDeploymentRequestDto basicSpringBootDeploymentRequestDto, @PathVariable String projectResourceId) {
        deploymentService.createBasicSpringBootDeployment(projectResourceId, basicSpringBootDeploymentRequestDto);
    }

    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @GetMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot/{deploymentResourceId}")
    public BasicSpringBootDeploymentResponseDto getBasicSpringBootDeployment(@PathVariable String projectResourceId, @PathVariable String deploymentResourceId) {
        return deploymentService.getBasicSpringBootDeployment(projectResourceId, deploymentResourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        String.format("Deployment with id %s not found.", deploymentResourceId)));
    }

    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @DeleteMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}")
    public void deleteDeployment(@PathVariable String projectResourceId, @PathVariable String deploymentResourceId) {
        deploymentService.deleteDeployment(projectResourceId, deploymentResourceId);
    }

    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @GetMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot/list")
    public List<BasicSpringBootDeploymentResponseDto> listAllBasicSpringBootDeployments(@PathVariable String projectResourceId) {
        return deploymentService.listAllBasicSpringBootDeployments(projectResourceId);
    }
}
