package org.zigmoi.ketchup.release.services;

import org.zigmoi.ketchup.release.dtos.DeploymentDetailsDto;
import org.zigmoi.ketchup.release.dtos.DeploymentRequestDto;
import org.zigmoi.ketchup.release.dtos.DeploymentResponseDto;
import org.zigmoi.ketchup.release.entities.DeploymentEntity;
import org.zigmoi.ketchup.release.entities.PipelineResource;
import org.zigmoi.ketchup.release.entities.Release;
import org.zigmoi.ketchup.release.entities.ReleaseId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReleaseService {

    String create(String deploymentResourceId);
    void rollback(String releaseResourceId); //rollback current release to specified version.
    void stop(String releaseResourceId);
    Release findById(String releaseResourceId);
    void delete(String releaseResourceId);
    void deleteDeployment(String projectResourceId, String deploymentResourceId);
    void update(Release release);
    Optional<Release> getActiveRelease(String deploymentResourceId);
    Set<Release> listAllInDeployment(String deploymentResourceId);
    Set<Release> listAllInProjectWithStatus(String projectResourceId, String status);
    Set<Release> listAllInProject(String projectResourceId);
    Set<Release> listRecentInProject(String projectResourceId);
    Set<PipelineResource> listAllPipelineResources(String releaseResourceId);
    PipelineResource getPipelineResourceById(String pipelineResourceId);

    DeploymentDetailsDto extractDeployment(Release release);

    void cleanPipelineResources(ReleaseId releaseId);

    String generateGitWebhookListenerURL(String vendor, String deploymentResourceId);

    Optional<Release> refreshReleaseStatus(String releaseResourceId);

    void updateDeploymentStatus(String projectResourceId, String deploymentResourceId, String status);
    void updateDeploymentDisplayName(String projectResourceId, String deploymentResourceId, String displayName);
    String createDeployment(String projectResourceId, DeploymentRequestDto deploymentRequestDto);
    DeploymentDetailsDto getDeployment(String deploymentResourceId);
    DeploymentResponseDto getDeploymentDetails(String deploymentResourceId);
    List<DeploymentEntity> listAllDeployments(String projectResourceId);
    void updateDeployment(String projectResourceId, String deploymentResourceId, DeploymentRequestDto deploymentRequestDto);
}
