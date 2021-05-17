package org.zigmoi.ketchup.application.dtos;

import lombok.Data;
import org.zigmoi.ketchup.application.entities.ApplicationId;

@Data
public class ApplicationDetailsDto {
    private ApplicationId applicationId;
    private String displayName;
    private String description;
    private String serviceName;
    private String serviceType;
    private String appServerPort;
    private String applicationType;
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
    private String containerRegistryType;
    private String containerRegistryUrl;
    private String containerRegistryUsername;
    private String containerRegistryPassword;
    private String containerRepositoryName;
    private String containerRegistryRedisUrl;
    private String containerRegistryRedisPassword;
    private String containerImageName;
    private String buildTool; // buildToolType is from settings which build tool this setting is for, whereas buildTool is selected in create application.
    private String buildToolType;
    private String buildToolSettingsData;
    private String baseBuildPath;
    private String buildToolSettingId;
    private String deploymentPipelineType;
    private String devKubernetesClusterSettingId;
    private String devKubernetesBaseAddress;
    private String devKubeconfig;
    private String devKubernetesNamespace;
    private String prodKubernetesClusterSettingId;
    private String prodKubeconfig;
    private String prodKubernetesNamespace;
    private String gunicornAppLocation;
    private String dotnetcoreProjectLocation;
}
