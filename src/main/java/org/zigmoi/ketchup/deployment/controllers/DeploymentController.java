package org.zigmoi.ketchup.deployment.controllers;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1PodList;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.common.GitUtility;
import org.zigmoi.ketchup.common.KubernetesUtility;
import org.zigmoi.ketchup.common.StringUtility;
import org.zigmoi.ketchup.deployment.dtos.DeploymentDetailsDto;
import org.zigmoi.ketchup.deployment.dtos.DeploymentRequestDto;
import org.zigmoi.ketchup.deployment.dtos.DeploymentResponseDto;
import org.zigmoi.ketchup.deployment.dtos.GitRepoConnectionTestRequestDto;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.services.DeploymentService;
import org.zigmoi.ketchup.project.dtos.settings.KubernetesClusterSettingsRequestDto;
import org.zigmoi.ketchup.release.services.ReleaseService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class DeploymentController {

    @Autowired
    private DeploymentService deploymentService;

    @PostMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot")
    public void createBasicSpringBootDeployment(@RequestBody DeploymentRequestDto deploymentRequestDto, @PathVariable String projectResourceId) {
        // deploymentRequestDto.setApplicationType(DeploymentConstants.APP_TYPE_BASIC_SPRING_BOOT);
        deploymentService.createDeployment(projectResourceId, deploymentRequestDto);
    }

    @GetMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}/instances")
    public List<String> getDeploymentInstances(@PathVariable String deploymentResourceId) {
        //TODO find active version of deployment and use its kubeconfig to get instances.
        DeploymentDetailsDto deploymentDetailsDto = deploymentService.getDeployment(deploymentResourceId);
        String kubeConfig = StringUtility.decodeBase64(deploymentDetailsDto.getDevKubeconfig());
        try {
            String labelSelector = "app.kubernetes.io/instance=release-".concat(deploymentResourceId);
            V1PodList res = KubernetesUtility.listPods(labelSelector, deploymentDetailsDto.getDevKubernetesNamespace(), "false", kubeConfig);
            System.out.println(res);
            return res.getItems().stream().map(pod -> pod.getMetadata().getName()).collect(Collectors.toList());
        } catch (IOException | ApiException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get instances.");
        }
    }

    @GetMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot/{deploymentResourceId}")
    public DeploymentResponseDto getBasicSpringBootDeployment(@PathVariable String projectResourceId, @PathVariable String deploymentResourceId) {
        // generateToken();
        return deploymentService.getDeploymentDetails(deploymentResourceId);
    }

    @PutMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}")
    public void updateDeployment(@PathVariable String projectResourceId, @PathVariable String deploymentResourceId, @RequestBody DeploymentRequestDto deploymentRequestDto) {
        //TODO provide option to update current deployment with new changes.
        deploymentService.updateDeployment(projectResourceId, deploymentResourceId, deploymentRequestDto);
    }

    @PutMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}/status/{status}")
    public void updateDeploymentStatus(@PathVariable("status") String status, @PathVariable String deploymentResourceId, @PathVariable String projectResourceId) {
        deploymentService.updateDeploymentStatus(projectResourceId, deploymentResourceId, status);
    }

    @PutMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}/displayName/{displayName}")
    public void updateDeploymentDisplayName(@PathVariable("deploymentResourceId") String deploymentResourceId, @PathVariable("displayName") String displayName, @PathVariable String projectResourceId) {
        deploymentService.updateDeploymentDisplayName(projectResourceId, deploymentResourceId, displayName);
    }

//    @DeleteMapping("v1/project/{projectResourceId}/deployments/{deploymentResourceId}")
//    public void deleteDeployment(@PathVariable String projectResourceId, @PathVariable String deploymentResourceId) {
//        deploymentService.deleteDeployment(projectResourceId, deploymentResourceId);
//        releaseService.deleteDeployment(deploymentResourceId);
//    }

    @GetMapping("v1/project/{projectResourceId}/deployments/basic-spring-boot/list")
    public List<DeploymentEntity> listAllBasicSpringBootDeployments(@PathVariable String projectResourceId) {
        return deploymentService.listAllBasicSpringBootDeployments(projectResourceId);
    }

    @PostMapping("v1/project/test-connection/git-remote/basic-auth")
    public Map<String, String> testConnectionGitRemoteBasicAuth(@RequestBody GitRepoConnectionTestRequestDto requestDto) {
        boolean connectionSuccessful = false;
        try {
            connectionSuccessful = GitUtility.instance().testConnection(requestDto.getUsername(), requestDto.getPassword(), requestDto.getRepoUrl());
        } catch (Exception e) {
            connectionSuccessful = false;
        }
        Map<String, String> status = new HashMap<>();
        status.put("status", connectionSuccessful ? "success" : "failed");
        return status;
    }

    @PutMapping("v1/project/test-connection/kubernetes-cluster/kubeconfig-auth")
    public Map<String, String> testConnectionKubernetesClusterKubeConfigAuth(@RequestBody KubernetesClusterSettingsRequestDto request) {
        boolean connectionSuccessful = false;
        try {
            String kubeConfig = StringUtility.decodeBase64(request.getFileData());
            connectionSuccessful = KubernetesUtility.testConnection(kubeConfig);
         } catch (ApiException | IOException  e) {
            connectionSuccessful = false;
        }
        Map<String, String> status = new HashMap<>();
        status.put("status", connectionSuccessful ? "success" : "failed");
        return status;
    }
}
