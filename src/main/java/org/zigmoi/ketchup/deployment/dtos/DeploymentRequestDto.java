package org.zigmoi.ketchup.deployment.dtos;

import lombok.Data;

@Data
public class DeploymentRequestDto {

    private String displayName;
    private String description;
    private String serviceName;
    private String appServerPort;
    private String replicas;
    private String gitRepoUrl;
    private String gitRepoUsername;
    private String gitRepoPassword;
    private String gitRepoBranchName;
    private String continuousDeployment;
    private String gitRepoPollingInterval;
    private String platform;
    private String containerRegistrySettingId;
    private String containerRegistryPath;
    private String buildTool;
    private String baseBuildPath;
    private String buildToolSettingId;
    private String deploymentPipelineType;
    private String devKubernetesClusterSettingId;
    private String devKubernetesNamespace;
    private String prodKubernetesClusterSettingId;
    private String prodKubernetesNamespace;
}
