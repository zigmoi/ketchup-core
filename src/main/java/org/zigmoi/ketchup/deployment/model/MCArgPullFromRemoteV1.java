package org.zigmoi.ketchup.deployment.model;

// MCArg -> Model Command Arg

import lombok.Data;

import java.io.File;

@Data
public class MCArgPullFromRemoteV1 {
    private String vendor, url, basePath, repoName, username, password;

    public File getRepoPath() {
        return new File(basePath + File.separator + repoName);
    }
}
