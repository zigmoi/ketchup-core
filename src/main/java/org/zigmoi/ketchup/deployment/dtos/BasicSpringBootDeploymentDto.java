package org.zigmoi.ketchup.deployment.dtos;

public class BasicSpringBootDeploymentDto {

    private String id;
    private String uniqueServiceName;

    private String gitProviderSettingId;
    private String buildToolSettingId;
    private String cloudProviderSettingId;
    private String containerRegistrySettingId;
    private String kubernetesClusterSettingId;
    private String kubernetesNamespaceSettingId;
    private String externalResourceIpHostnameMappingSettingId;

    private String gitRepoName;
    private String gitRepoCommitId;
    private String gitRepoBranchName;

    private String buildDirectory;
    private String dockerImageName;

    private int appServerPort;
    private int appGeneratedJarName;
    private int appTimezone;
    private int appBasePath;
}
