package org.zigmoi.ketchup.application.services;

import org.springframework.validation.annotation.Validated;
import org.zigmoi.ketchup.application.dtos.ApplicationDetailsDto;
import org.zigmoi.ketchup.application.dtos.ApplicationRequestDto;
import org.zigmoi.ketchup.application.dtos.ApplicationResponseDto;
import org.zigmoi.ketchup.application.entities.*;
import org.zigmoi.ketchup.common.validations.ValidProjectId;
import org.zigmoi.ketchup.common.validations.ValidResourceId;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Validated
public interface ApplicationService {

    String createRevision( @NotBlank @Pattern(regexp = "GIT WEBHOOK|MANUAL")String trigger,
                           @Size(max = 100) String commitId,
                           @Valid ApplicationId applicationId);

    void rollbackToRevision(@Valid RevisionId revisionId); //rollback current release to specified version.

    void stopRevisionPipeline(@Valid RevisionId revisionId);

    Revision findRevisionById(@Valid RevisionId revisionId);

    Revision findRevisionByResourceId(@ValidResourceId String revisionResourceId);

    void deleteRevision(@Valid RevisionId revisionId);

    void deleteApplication(@Valid ApplicationId applicationId);

    void updateRevision(@Valid Revision revision);

    Optional<Revision> getCurrentRevision(@Valid ApplicationId applicationId);

    Optional<Revision> getLastSuccessfulRevision(@Valid ApplicationId applicationId);

    Set<Revision> listAllRevisionsInApplication(@Valid ApplicationId applicationId);

    Set<Revision> listAllRevisionPipelinesInProjectWithStatus(@ValidProjectId String projectResourceId,
                                                              @NotBlank  @Pattern(regexp = "IN PROGRESS|SUCCESS|FAILED") String status);

    Set<Revision> listAllRevisionsInProject(@ValidProjectId String projectResourceId);

    List<Revision> listRecentRevisionPipelinesInProject(@ValidProjectId String projectResourceId);

    Set<PipelineArtifact> listAllPipelineArtifactsInRevision(@Valid RevisionId revisionId);

    PipelineArtifact getPipelineArtifactsById(@Valid PipelineArtifactId pipelineArtifactId);

    ApplicationDetailsDto extractApplicationByRevisionId(@Valid Revision revision);

    void cleanPipelineResourcesInRevision(@Valid RevisionId revisionId);

    String generateGitWebhookListenerURL(@NotBlank String vendor, @Valid ApplicationId applicationId);

    Optional<Revision> refreshRevisionStatus(@Valid RevisionId revisionId);

    String createApplication(@ValidProjectId String projectResourceId, @Valid ApplicationRequestDto applicationRequestDto);

    ApplicationDetailsDto getApplication(@Valid ApplicationId applicationId);

    ApplicationResponseDto getApplicationDetails(@Valid ApplicationId applicationId);

    List<Application> listAllApplicationsInProject(@ValidProjectId String projectResourceId);

    void updateApplication(@ValidProjectId String projectResourceId, @ValidResourceId String applicationResourceId, @Valid ApplicationRequestDto applicationRequestDto);

    Map<String, Long> getDashboardDataForProject(@ValidProjectId String projectResourceId);
}
