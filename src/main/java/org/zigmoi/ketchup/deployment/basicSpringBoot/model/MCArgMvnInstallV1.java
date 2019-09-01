package org.zigmoi.ketchup.deployment.basicSpringBoot.model;

import lombok.Data;

@Data
public class MCArgMvnInstallV1 {
    private String basePath, repoName, buildPath, mvnCommandPath, privateRepoSettingsPath, branchName, commitId;
}
