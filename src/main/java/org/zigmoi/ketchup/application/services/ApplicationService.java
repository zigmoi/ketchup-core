package org.zigmoi.ketchup.application.services;

import org.zigmoi.ketchup.application.dtos.ApplicationDetailsDto;
import org.zigmoi.ketchup.application.dtos.ApplicationRequestDto;
import org.zigmoi.ketchup.application.dtos.ApplicationResponseDto;
import org.zigmoi.ketchup.application.entities.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ApplicationService {

    String create(ApplicationId applicationId);
    void rollback(RevisionId revisionId); //rollback current release to specified version.
    void stop(RevisionId revisionId);
    Revision findById(RevisionId revisionId);
    Revision findByReleaseResourceId(String releaseResourceId);
    void delete(RevisionId revisionId);
    void deleteDeployment(ApplicationId applicationId);
    void update(Revision revision);
    Optional<Revision> getActiveRelease(ApplicationId applicationId);
    Set<Revision> listAllInDeployment(ApplicationId applicationId);
    Set<Revision> listAllInProjectWithStatus(String projectResourceId, String status);
    Set<Revision> listAllInProject(String projectResourceId);
    List<Revision> listRecentInProject(String projectResourceId);
    Set<PipelineArtifact> listAllPipelineResources(RevisionId revisionId);
    PipelineArtifact getPipelineResourceById(PipelineArtifactId pipelineArtifactId);
    ApplicationDetailsDto extractDeployment(Revision revision);
    void cleanPipelineResources(RevisionId revisionId);
    String generateGitWebhookListenerURL(String vendor, ApplicationId applicationId);
    Optional<Revision> refreshReleaseStatus(RevisionId revisionId);
    String createDeployment(String projectResourceId, ApplicationRequestDto applicationRequestDto);
    ApplicationDetailsDto getDeployment(ApplicationId applicationId);
    ApplicationResponseDto getDeploymentDetails(ApplicationId applicationId);
    List<Application> listAllDeployments(String projectResourceId);
    void updateDeployment(String projectResourceId, String deploymentResourceId, ApplicationRequestDto applicationRequestDto);
}
