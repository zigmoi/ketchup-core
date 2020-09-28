package org.zigmoi.ketchup.deployment.services;

import org.zigmoi.ketchup.deployment.dtos.*;
import org.zigmoi.ketchup.deployment.entities.DeploymentEntity;
import org.zigmoi.ketchup.deployment.entities.DeploymentId;

import java.util.List;
import java.util.Optional;

public interface DeploymentService {

    void updateDeploymentStatus(String projectResourceId, String deploymentResourceId, String status);
    void updateDeploymentDisplayName(String projectResourceId, String deploymentResourceId, String displayName);
    String createDeployment(String projectResourceId, DeploymentRequestDto deploymentRequestDto);
    DeploymentDetailsDto getDeployment(String deploymentResourceId);
    DeploymentResponseDto getDeploymentDetails(String deploymentResourceId);
    String createBasicSpringBootDeployment(String projectResourceId, BasicSpringBootDeploymentRequestDto basicSpringBootDeploymentRequestDto);
    Optional<BasicSpringBootDeploymentResponseDto> getBasicSpringBootDeployment(String projectResourceId, String deploymentResourceId);
    void deleteDeployment(String projectResourceId, String deploymentResourceId);
    List<DeploymentEntity> listAllBasicSpringBootDeployments(String projectResourceId);

    void updateDeployment(String projectResourceId, String deploymentResourceId, DeploymentRequestDto deploymentRequestDto);
}
