package org.zigmoi.ketchup.deployment.services;

import org.zigmoi.ketchup.deployment.dtos.BasicSpringBootDeploymentDto;

import java.util.List;
import java.util.Optional;

public interface DeploymentService {
    void createBasicSpringBootDeployment(BasicSpringBootDeploymentDto basicSpringBootDeploymentDto);
    void updateDeploymentStatus(String deploymentId, String status);
    void updateDeploymentDisplayName(String deploymentId, String displayName);
    Optional<BasicSpringBootDeploymentDto> getBasicSpringBootDeployment(String deploymentId);
    void deleteDeployment(String deploymentId);
    List<BasicSpringBootDeploymentDto> listAllBasicSpringBootDeployments();
}
