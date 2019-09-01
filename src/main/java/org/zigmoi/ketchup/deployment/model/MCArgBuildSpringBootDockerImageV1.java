package org.zigmoi.ketchup.deployment.model;

import lombok.Data;

@Data
public class MCArgBuildSpringBootDockerImageV1 {
    private String basePath, repoName, buildPath, privateRepoSettingsPath, branchName, commitId;
    private String dockerRepoBaseUrl, dockerRepoUsername, dockerRepoPassword;
    private String dockerBuildBaseImage, dockerBuildImageName, dockerBuildImageTag;
    private String javaArgs;
    private int port;
    private String timezone;
}
