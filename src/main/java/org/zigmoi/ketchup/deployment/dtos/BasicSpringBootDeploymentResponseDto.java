package org.zigmoi.ketchup.deployment.dtos;

public class BasicSpringBootDeploymentResponseDto {

    private String id;
    private String serviceName;

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
    private String gitRepoToBuildDirectory;

    private String dockerImageName;

    private int appServerPort;
    private int appTimezone;
    private int appBasePath;
}
