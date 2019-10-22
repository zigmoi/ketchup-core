package org.zigmoi.ketchup.deployment.services;

import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentRequestDto;
import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentResponseDto;

import java.util.List;
import java.util.Optional;

public interface DeploymentService {

    void updateDeploymentStatus(String projectResourceId, String deploymentResourceId, String status);
    void updateDeploymentDisplayName(String projectResourceId, String deploymentResourceId, String displayName);

    String createBasicSpringBootDeployment(String projectResourceId, BasicSpringBootDeploymentRequestDto basicSpringBootDeploymentRequestDto);
    Optional<BasicSpringBootDeploymentResponseDto> getBasicSpringBootDeployment(String projectResourceId, String deploymentResourceId);
    void deleteDeployment(String projectResourceId, String deploymentResourceId);
    List<BasicSpringBootDeploymentResponseDto> listAllBasicSpringBootDeployments(String projectResourceId);
}
