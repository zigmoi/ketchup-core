package org.zigmoi.ketchup.application.dtos;

import lombok.Data;
import org.zigmoi.ketchup.application.entities.ApplicationId;

@Data
public class ApplicationResponseDto {
    private ApplicationId applicationId;
    private String displayName;
    private String description;
    private String applicationType;
    private String serviceName;
    private String serviceType;
    private String appServerPort;
    private String replicas;
    private String deploymentStrategy;
    private String gitRepoUrl;
    private String gitRepoUsername;
    private String gitRepoPassword;
    private String gitRepoBranchName;
    private String continuousDeployment;
    private String gitRepoPollingInterval;
    private String platform;
    private String containerRegistrySettingId;
    private String containerImageName;
    private String buildTool;
    private String baseBuildPath;
    private String buildToolSettingId;
    private String deploymentPipelineType;
    private String devKubernetesClusterSettingId;
    private String devKubernetesBaseAddress;
    private String devKubernetesNamespace;
    private String prodKubernetesClusterSettingId;
    private String prodKubernetesNamespace;
    private String gunicornAppLocation;
    private String dotnetcoreProjectLocation;
    private DeploymentStatus deploymentStatus;
}
