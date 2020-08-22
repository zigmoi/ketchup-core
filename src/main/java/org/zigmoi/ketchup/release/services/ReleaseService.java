package org.zigmoi.ketchup.release.services;

import org.zigmoi.ketchup.deployment.dtos.DeploymentDetailsDto;
import org.zigmoi.ketchup.release.entities.PipelineResource;
import org.zigmoi.ketchup.release.entities.Release;

import java.util.Set;

public interface ReleaseService {

    void create(String deploymentResourceId);
    void rollback(String deploymentResourceId); //rollback current release to previous version.
    void stop(String releaseResourceId);
    Release findById(String releaseResourceId);
    void delete(String releaseResourceId);
    void update(Release release);
    Set<Release> listAllInDeployment(String deploymentResourceId);
    Set<Release> listAllInProject(String projectResourceId);
    Set<PipelineResource> listAllPipelineResources(String releaseResourceId);
    PipelineResource getPipelineResourceById(String pipelineResourceId);

    DeploymentDetailsDto extractDeployment(Release release);
}
