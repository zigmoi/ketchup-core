package org.zigmoi.ketchup.deployment.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentDto;
import org.zigmoi.ketchup.deployment.services.DeploymentService;

import java.util.List;

@RestController
public class DeploymentController {

    @Autowired
    private DeploymentService deploymentService;

    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @PostMapping("/v1/deployment/basic-spring-boot-app")
    public void createBasicSpringBootDeployment(@RequestBody BasicSpringBootDeploymentDto basicSpringBootDeploymentDto) {
        deploymentService.createBasicSpringBootDeployment(basicSpringBootDeploymentDto);
    }

    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @PutMapping("/v1/deployment/{id}/status/{status}")
    public void updateDeploymentStatus(@PathVariable("id") String deploymentId, @PathVariable("status") String status) {
        deploymentService.updateDeploymentStatus(deploymentId, status);
    }

    @PreAuthorize("hasAnyRole('ROLE_PROJECT_MEMBER')")
    @PutMapping("/v1/tenant/{id}/displayName/{displayName}")
    public void updateDeploymentDisplayName(@PathVariable("id") String deploymentId, @PathVariable("displayName") String displayName) {
        deploymentService.updateDeploymentDisplayName(deploymentId, displayName);
    }

    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @GetMapping("/v1/basic-spring-boot-app/{id}")
    public BasicSpringBootDeploymentDto getBasicSpringBootDeployment(@PathVariable("id") String deploymentId) {
        return deploymentService.getBasicSpringBootDeployment(deploymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        String.format("Deployment with id %s not found.", deploymentId)));
    }

    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @DeleteMapping("/v1/deployment/{id}")
    public void deleteDeployment(@PathVariable("id") String deploymentId) {
        deploymentService.deleteDeployment(deploymentId);
    }

    @PreAuthorize("hasRole('ROLE_PROJECT_MEMBER')")
    @GetMapping("/v1/deployments/basic-spring-boot-app")
    public List<BasicSpringBootDeploymentDto> listAllBasicSpringBootDeployments() {
        return deploymentService.listAllBasicSpringBootDeployments();
    }
}
